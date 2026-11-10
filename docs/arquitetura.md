# Arquitetura — backend

O desenho das camadas e o que cada uma decide. `docs/padroes.md` é a regra de
como escrever; este arquivo é o porquê da forma. Se os dois divergirem, o
errado é este — a regra é lida antes de escrever, o desenho depois.

---

## Visão geral

```
  navegador ──▶ React (frontend/)  ·  Swagger UI (/docs)
                        │ HTTP/JSON, cookie JSESSIONID + X-XSRF-TOKEN
                        ▼
  ┌────────────────────────────────────────────────────────────────┐
  │  SecurityFilterChain   CSRF por cookie · sessão · 401 sem corpo │
  └──────────────┬─────────────────────────────────────────────────┘
                 ▼
  Controller   Usuario · Sessao · Tarefa · Categoria · Painel
               @Valid, status, Location; dono vem de UsuarioAtual
                 │  DTO
                 ▼
  Service      UsuarioService · TarefaService · CategoriaService · PainelService
               regra, pertencimento (404), campos derivados, Clock injetado
                 │  Model                       ▲
                 ▼                              │
  Mapper  DTO ↔ Model          Repository   Usuario · Tarefa · Categoria (Spring Data)
                                            PainelRepository (MongoTemplate, $lookup)
                                                │ BSON
                                                ▼
                     MongoDB — usuarios · categorias{anotacoes[]} · tarefas
                     $jsonSchema fechado nas três

  transversal:  GlobalExceptionHandler  @RestControllerAdvice → ApiError
                MongoConfiguration      conversores de enum e _class desligado
                SecurityConfiguration   cadeia, BCrypt, CSRF, estratégia de sessão
```

Uma camada só conhece a de baixo. O Controller não fala com o Repository, o
Service não sabe que existe HTTP, e o Model nunca atravessa a fronteira HTTP —
é por isso que existem quatro DTOs para uma entidade.

## Um POST, do começo ao fim

Rastrear uma requisição é o jeito mais curto de ver de quem é cada decisão.

`POST /api/tarefas` com `{"titulo":"Estudar NoSQL","prazo":"2026-09-20","prioridade":"alta","observacao":"Revisar o capítulo 4"}`

| # | Onde | O que acontece | Decisão de quem |
|---|---|---|---|
| 1 | Jackson | JSON → `TarefaCreateRequest`. `"alta"` vira `Prioridade.ALTA` pelo `@JsonCreator`. Slug inválido estoura aqui, antes da validação. | `model/Prioridade` |
| 2 | Controller | `@Valid` dispara o Jakarta Validation. Campo faltando → `MethodArgumentNotValidException`, e o fluxo pula para o passo 8. | `dto/TarefaCreateRequest` |
| 3 | Mapper | `toModel` monta a `Tarefa` com **os quatro campos que vieram do cliente**. `concluida`, `criadaEm` e `atualizadaEm` ficam intocados. | `mapper/TarefaMapper` |
| 4 | Service | `concluida = false`, `criadaEm = atualizadaEm = Instant.now(clock)`. É aqui que nasce tudo que o cliente não manda. | `service/TarefaService` |
| 5 | Repository | `save`. O Spring Data converte `Prioridade` → `"alta"` e **não** grava `_class`. | `configuration/MongoConfiguration` |
| 6 | MongoDB | O `$jsonSchema` aceita ou recusa. Campo extra ou tipo errado falha na escrita. | `banco/init.js` |
| 7 | Controller | `toResponse` → `201` com header `Location` apontando para o recurso. | `controller/TarefaController` |
| 8 | Advice | Qualquer exceção vira `ApiError`. Validação de campo leva o mapa `fieldErrors`. | `exception/GlobalExceptionHandler` |

A leitura é o mesmo caminho invertido, com uma diferença que vale nota: a
**listagem usa outro DTO**. `GET /api/tarefas` devolve `TarefaSummaryResponse`
(seis campos) e `GET /api/tarefas/{id}` devolve `TarefaResponse` (oito). Um
DTO por caso de uso é o que torna a projeção uma decisão explícita em vez de um
acidente de serialização.

## A fronteira que causa discussão

Mapper e Service são a única fronteira onde dois autores divergem na prática, e
a regra que a resolve é uma frase:

> **O Mapper só traduz o que veio do cliente. Tudo que é derivado nasce no
> Service.**

As consequências visíveis no código:

- `concluida = false` na criação é **regra de negócio**, não valor-padrão de
  campo. Mora no Service, e `TarefaCreateRequest` nem tem o campo — o cliente
  não pode criar uma tarefa já concluída.
- `criadaEm` e `atualizadaEm` nascem no Service, de um `Clock` injetado.
- `updateModel` altera **a instância que veio do banco**, em vez de construir
  uma nova. É isso que preserva o `id` e os carimbos de tempo — `criadaEm` não
  está em `TarefaUpdateRequest`, então a omissão não pode apagá-lo.

## O relógio é injetado

`ClockConfiguration` expõe um `Clock` em vez de o Service chamar
`Instant.now()`. O motivo é testabilidade com valor exato: com um
`Clock.fixed`, o teste afirma `criadaEm == T`, não `criadaEm != null`.

Tem um segundo motivo, achado na prática: no Windows duas chamadas seguidas a
`Instant.now()` podem devolver o mesmo valor, porque a granularidade do relógio
do sistema é maior que o intervalo entre elas. Um teste que comparasse
`atualizadaEm > criadaEm` seria instável. Com o relógio injetado, o teste
escolhe os dois instantes.

## O caminho de erro é único

Nenhum `try/catch` de fluxo normal em lugar nenhum. O Service lança, o
Controller não vê, e o `@RestControllerAdvice` transforma em `ApiError`:

| Exceção | Status | `fieldErrors` |
|---|---|---|
| `RecursoNaoEncontradoException` (Tarefa, Categoria, Anotacao) | `404` | vazio |
| `EmailJaCadastradoException` | `409` | vazio |
| `DuplicateKeyException` (corrida no índice único de e-mail) | `409` | vazio |
| `BadCredentialsException` (login com senha errada) | `401` | vazio |
| `MethodArgumentNotValidException` | `400` | um par por campo inválido |
| `HttpMessageNotReadableException` | `400` | vazio |

`fieldErrors` vem **vazio, nunca nulo**, para o cliente não precisar checar
antes de iterar.

A exceção à regra é a requisição sem sessão: ela morre na cadeia de filtros,
antes de existir Controller, e sai como `401` **sem corpo**. O `401` de senha
errada passa pelo Controller e tem `ApiError`. Os dois formatos são esperados.

O último caso merece explicação: prioridade com slug desconhecido falha na
desserialização, antes de o Bean Validation rodar. Por isso
`TarefaCreateRequest` não tem validador de prioridade — o `enum` já é o
validador, e o erro chega como corpo ilegível.

## Onde a infraestrutura se esconde

Duas classes em `configuration/` existem só porque o framework não faz o que o
banco exige. Estão excluídas da cobertura por isso: são fiação, não
comportamento.

- **`MongoConfiguration`** faz duas coisas. Desliga a escrita do campo `_class`
  (o `$jsonSchema` é fechado com `additionalProperties: false`, então o campo
  extra faria todo insert falhar) e registra o par de conversores
  `Prioridade ↔ slug` (o Spring Data persistiria `"BAIXA"`, o nome da
  constante, e o banco espera `"baixa"`).
- **`AepApplication`** fixa o fuso do processo em UTC antes de subir o Spring.
  `prazo` é data de calendário gravada à meia-noite UTC, e o conversor padrão
  do Spring Data usa `ZoneId.systemDefault()` — sem essa linha a invariante
  seria falsa em qualquer máquina fora de UTC.

## Quem é o dono

A segurança está partida em duas perguntas, e cada uma tem dono:

- **"Quem é você?"** é do Spring Security. `SecurityConfiguration` deixa
  públicos só `POST /api/usuarios`, `POST /api/sessao`, o Swagger e os
  estáticos; todo o resto de `/api/**` exige sessão e responde `401` sem corpo.
- **"Isso é seu?"** é do Service. O Controller pega o id da sessão em
  `UsuarioAtual` e passa adiante; o Service busca por `id` **e** `usuarioId`, e
  recurso de outro dono vira a mesma exceção de recurso inexistente — `404`, não
  `403`, porque `403` confirmaria que o recurso existe. O mesmo vale para
  `categoriaId` alheio na tarefa.

O `POST /api/sessao` autentica à mão, fora dos filtros, então precisa chamar
sozinho o que o filtro de login faria: a `SessionAuthenticationStrategy` (troca o
id da sessão e descarta o token CSRF — contra fixação de sessão) e o
`SecurityContextRepository.saveContext` (sem ele a próxima requisição volta
`401`). Consequência visível para o cliente: a resposta do login apaga o cookie
`XSRF-TOKEN`, e o novo só chega na resposta seguinte.

## O painel

`GET /api/painel` é a única leitura que cruza coleções. `PainelRepository` parte
de `categorias` filtradas pelo dono e faz `$lookup` em `tarefas`; as contas
(atrasada, percentual, o balde "Sem disciplina") ficam no `PainelService`, onde
são testáveis sem MongoDB — a fronteira "prazo igual a hoje não é atrasada" tem
teste próprio.

A armadilha que quem continuar o projeto vai encontrar: as referências
(`usuarioId`, `categoriaId`) são gravadas como `string`, e `_id` é `ObjectId`. O
`$lookup` de quatro argumentos (`localField`/`foreignField`) compara tipos
diferentes e devolve array vazio **sem erro nenhum**. Por isso o estágio usa
`let` + `$expr` + `$toString`. `PainelApiIT` prova o resultado contra MongoDB
real.

## Empacotamento

Um processo Spring Boot serve a API (`/api/**`) e o Swagger UI (`/docs`, com o
OpenAPI em `/v3/api-docs`). O React de `frontend/` roda no Vite, em
desenvolvimento. O MongoDB roda em contêiner, pelo `docker-compose.yml` da raiz,
ou instalado na máquina.

O `crud.html` da 1ª entrega continua em `static/`, mas ficou atrás da
autenticação e responde `401`: ele não sabe fazer login.

## O que a arquitetura ainda não tem

Declarado aqui porque limite escondido vira pergunta na apresentação.

- **Sessão em memória.** Reiniciar o servidor desloga todo mundo.
- **Última escrita vence.** Duas edições simultâneas se sobrescrevem. Bloqueio
  otimista por versão resolve sem mexer na modelagem.
- **Excluir disciplina não é atômico.** Desvincula as tarefas e depois apaga a
  disciplina, em duas escritas, num MongoDB standalone sem transação. A ordem
  garante que a falha no meio deixe tarefa sem disciplina, nunca tarefa
  apontando para disciplina inexistente.
- **Sem paginação.** A listagem carrega tudo do dono. Para a PoC cabe com folga.
