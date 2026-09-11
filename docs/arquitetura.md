# Arquitetura — backend

O desenho das camadas e o que cada uma decide. `docs/padroes.md` é a regra de
como escrever; este arquivo é o porquê da forma. Se os dois divergirem, o
errado é este — a regra é lida antes de escrever, o desenho depois.

---

## Visão geral

```
                       ┌──────────────────────────────┐
  navegador ──────────▶│  crud.html  (static/)        │
                       │  Swagger UI  (/docs)         │
                       └──────────────┬───────────────┘
                                      │ HTTP/JSON
                                      ▼
  ┌────────────────────────────────────────────────────────────────┐
  │  TarefaController          @RestController  /api/tarefas       │
  │    @Valid, status, header Location                             │
  └──────────────┬─────────────────────────────────────────────────┘
                 │  DTO                               ▲  DTO
                 ▼                                    │
  ┌────────────────────────────────────────────────────────────────┐
  │  TarefaService             @Service                            │
  │    regra, campos derivados, TarefaNotFoundException            │
  │    ◀── Clock (ClockConfiguration)                              │
  └──────────────┬─────────────────────────────────────────────────┘
                 │  Model          ▲  Model
                 ▼                 │
       ┌─────────────────┐   ┌─────────────────────────────────┐
       │  TarefaMapper   │   │  TarefaRepository               │
       │  DTO ↔ Model    │   │  MongoRepository<Tarefa,String> │
       └─────────────────┘   └──────────────┬──────────────────┘
                                            │ BSON
                                            ▼
                             ┌──────────────────────────────┐
                             │  MongoDB — coleção `tarefas` │
                             │  $jsonSchema (fechado)       │
                             └──────────────────────────────┘

  transversal:  GlobalExceptionHandler  @RestControllerAdvice → ApiError
                MongoConfiguration      conversores e _class desligado
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

Nenhum `try/catch` de fluxo normal em lugar nenhum. O Service lança
`TarefaNotFoundException`, o Controller não a vê, e o
`@RestControllerAdvice` transforma em `ApiError`:

| Exceção | Status | `fieldErrors` |
|---|---|---|
| `TarefaNotFoundException` | `404` | vazio |
| `MethodArgumentNotValidException` | `400` | um par por campo inválido |
| `HttpMessageNotReadableException` | `400` | vazio |

`fieldErrors` vem **vazio, nunca nulo**, para o cliente não precisar checar
antes de iterar — o `crud.html` depende disso.

O terceiro caso merece explicação: prioridade com slug desconhecido falha na
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

## Empacotamento

Um processo Spring Boot serve três coisas na mesma porta:

| Caminho | O que é |
|---|---|
| `/api/tarefas` | a API |
| `/crud.html` | o cliente mínimo, de `src/main/resources/static/` |
| `/docs` | Swagger UI, com o OpenAPI em `/v3/api-docs` |

Mesma origem, logo sem CORS e sem proxy. O MongoDB roda em contêiner separado,
pelo `docker-compose.yml` da raiz.

O React de `frontend/` **não** está nesse processo nesta entrega: ele roda no
Vite, consome mocks e não fala com a API. O build para dentro do `static/` está
planejado (D2 e T36 em `docs/backend-tarefas.md`) e é parte da evolução da 2ª
entrega.

## O que a arquitetura ainda não tem

Declarado aqui porque limite escondido vira pergunta na apresentação.

- **Nenhuma autenticação e nenhum dono.** A tarefa não tem campo de usuário. A
  coleção `usuarios` é a 2ª entrega, e criá-la agora violaria o teto de uma
  coleção só da seção 8 do enunciado.
- **Nenhum escopo por usuário.** `GET /api/tarefas` devolve a coleção inteira.
  Nenhum índice além do `_id`: índice sem consulta que o use é escrita mais
  lenta de graça.
- **Última escrita vence.** Duas edições simultâneas se sobrescrevem. Bloqueio
  otimista por versão resolve sem mexer na modelagem.
- **Sem paginação.** A listagem carrega tudo. Para a PoC cabe com folga.

## Para onde isso cresce

A 2ª entrega pede múltiplas coleções, relacionamento e um objeto complexo. A
arquitetura não muda de forma — ganha domínios, repetindo a mesma pilha:

```
usuarios    Usuario   → UsuarioRepository   → UsuarioService   → UsuarioController
categorias  Categoria → CategoriaRepository → CategoriaService → CategoriaController
            (com a lista de anotações aninhada — o objeto complexo exigido)
```

`usuarioId` entra na tarefa referenciando a coleção nova. É adição, não
reescrita — e é o que torna o critério "evolução verificável" visível em diff.
A especificação das coleções está em `docs/modelagem-banco.md`.
