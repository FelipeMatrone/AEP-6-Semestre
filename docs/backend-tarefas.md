# Backend — tarefas para a 1ª entrega

Levantamento a partir da comparação entre o projeto de referência do professor
(`AepMongoJava2026`, fora deste repositório) e o estado atual do nosso.

Referências normativas: `AEP_ESoft_6S.md` (enunciado) e o `AGENTS.md` /
`HARNESS.md` do projeto de referência — que são, na prática, o critério de
correção escrito.

As tarefas estão numeradas na **ordem de execução**: T1 antes de T2, sem
exceção. Se a ordem mudar, os números mudam junto.

---

## O que a referência estabelece

O projeto do professor não é só um exemplo de CRUD. Ele traz:

- `AGENTS.md` com 27 regras arquiteturais numeradas;
- `HARNESS.md` com uma *Definition of Done* de 8 itens;
- `.agents/skills/` com a ordem de implementação esperada.

O domínio dele é minúsculo de propósito — `Linguagem` tem quatro campos
escalares, coleção única, zero aninhamento. É a leitura literal da seção 8
aplicada ao próprio material de aula.

O que ele entrega e não temos:

| Item | Referência | Nosso repo |
|---|---|---|
| Backend | Spring Boot 3.5.16 / Java 21 | não existe |
| Camadas | Controller · DTO · Mapper · Service · Repository · Model · Exception · Configuration | — |
| Validação de entrada | Jakarta Validation nos Request DTOs | só Zod, no cliente |
| Erros | `@RestControllerAdvice` + `ApiError` com `fieldErrors` | — |
| Doc da API | Springdoc em `/docs` e `/v3/api-docs` | — |
| Testes | Service (Mockito), Controller (MockMvc), Integração (Testcontainers) | nenhum |
| Cobertura | JaCoCo, gate de 70% no `verify` | não medida |
| Cliente web | `crud.html` estático servido pelo Spring | React desconectado da API |
| Seed | classe de configuração idempotente, perfil `dev` | `init.js`, roda uma vez só |
| Docs | `architecture.md`, `decisions.md` (12 ADRs), `http-api.md` | `modelagem-banco.md`, README de uma linha |

### O que dessa lista rende nota — e o que não rende

A camada de processo da referência (`AGENTS.md`, `HARNESS.md`, `.agents/skills/`)
**não aparece em nenhum dos oito critérios da 1ª entrega nem dos nove da 2ª**.
Não pontua.

Mas "não pontua" não é "não serve", e aqui ela serve por um motivo que não é
nota: **o repositório tem mais de um autor e mais de uma ferramenta.** Na
ausência de regra escrita, um modelo imita o código ao redor — e enquanto o
backend não existe, não há o que imitar. Quem escrever o primeiro `Controller`
inventa um padrão. Convenção escrita antes de T8 é o que impede dois padrões
paralelos de nascerem em branches diferentes.

`AGENTS.md` é convenção **multi-ferramenta** (Codex, Cursor, Copilot, Gemini CLI,
Aider e outros leem esse arquivo). `CLAUDE.md` é lido só pelo Claude Code —
tudo que vive lá é invisível para quem usar outra coisa. Por isso as convenções
de arquitetura vão para `AGENTS.md`, e não o contrário.

O que rende é vizinho e passa batido:

| Artefato da referência | Rende nota? | Nosso caminho |
|---|---|---|
| `docs/architecture.md` | sim — "Documentação técnica" (0,1 na 2ª) e requisito 7 | T30 |
| `docs/http-api.md` | sim — mesmo critério; e sai quase de graça do Springdoc | T31 |
| `docs/decisions.md` | sim — sustenta "organização do código" e a defesa no vídeo | T29 |
| `src/main/resources/static/crud.html` | sim — "PoC executa o fluxo principal" e o vídeo | T25 |
| `AGENTS.md` | não — mas vale por ser multi-ferramenta | T5, **antes de T8** |
| `HARNESS.md` | não | comandos vão para o README (T28); DoD para o `AGENTS.md` (T5) |
| `.agents/skills/` | não | conteúdo vira `docs/padroes.md` (T6) e `docs/checklist-revisao.md` (T32) |
| `TODO.md` | não | já coberto por `frontend/TODO.md` + este arquivo |
| `docs/apresentacao.html` | indireto — o vídeo vale 0,3 | T34, opcional |

**Armadilha de formato:** `AGENTS.md` e `.agents/skills/` não são a mesma
decisão. O primeiro é convenção aberta, lida por praticamente toda ferramenta —
vale versionar. O segundo é layout do Codex (`SKILL.md` + `agents/openai.yaml`);
o Claude Code lê `.claude/skills/`. Nenhum diretório serve os dois, e manter o
mesmo conteúdo nos dois recria a dessincronia que se quer evitar. Some-se a isso
que **nenhum dos dois é visível para o professor abrindo o repositório** — ele
vê um diretório de configuração de ferramenta, não uma convenção de projeto.
Por isso o conteúdo procedural vira markdown comum em `docs/`, referenciado pelo
`AGENTS.md`: qualquer ferramenta chega lá por leitura, e qualquer pessoa também.

### Nomes de arquivo

Documentação nova em `docs/` usa nome em português, como os arquivos que já
existem lá (`modelagem-banco.md`, `sugestoes-projetos.md`): `arquitetura.md`,
`decisoes.md`, `api-http.md`, `padroes.md`, `checklist-revisao.md`. Não copiar
os nomes em inglês da referência.

---

## Decisões travadas nesta sessão

### D1 — Coleção única `tarefas`, documento achatado

A tarefa **é** o documento. Não há aninhamento na 1ª entrega.

```js
// coleção: tarefas
{
  _id: ObjectId,
  titulo: "Entregar a modelagem do banco",
  prazo: ISODate("2026-09-08T00:00:00Z"),
  prioridade: "alta",          // alta | media | baixa
  concluida: false,
  dono: "samuel@exemplo.com",
  criadaEm: ISODate(...),
  atualizadaEm: ISODate(...)
}
```

**Motivo:** o array de subdocumentos é literalmente o exemplo da 2ª entrega na
seção 8 do enunciado. Gastá-lo agora custa o critério "evolução verificável".
A referência do professor confirma a leitura restritiva.

**Custo aceito:** login e cadastro seguem mock na 1ª entrega. O frontend não é
entregável obrigatório do 1º bimestre, e o fluxo principal avaliado é gerenciar
tarefas, não autenticar.

**Caminho para a 2ª entrega:** `dono` (String) vira `usuarioId` referenciando a
coleção `usuarios` nova; `categorias` entra com anotações aninhadas. Três
coleções, dois relacionamentos, um objeto complexo — tudo aditivo, visível em
diff.

> `docs/modelagem-banco.md` descrevia o agregado `usuarios` com `tarefas[]`.
> Reescrito em 09/09/2026 (T27): agora especifica a coleção `tarefas` achatada,
> e o agregado virou a seção "Caminho para a 2ª entrega".

### D2 — Frontend buildado dentro do Spring

`npm run build` gera em `src/main/resources/static/`. Uma origem, uma porta,
zero CORS. O professor sobe o projeto com dois comandos.

Prioridade baixa para a 1ª entrega, já que o frontend não é obrigatório nela —
e substituído nela pelo caminho barato de D5.

### D3 — Cobertura medida só no backend, via JaCoCo

Gate de 70% no `./mvnw verify`, igual à referência. O frontend fica como cliente
da API e não entra na conta. Evidência reproduzível em um comando.

**Consequência que precisa estar escrita:** o enunciado diz que a cobertura é
"calculada sobre o código da PoC **apresentado naquele marco**". Se o React
está no repositório sem uma frase delimitando o escopo, o corretor pode contá-lo
e o item — que é eliminatório — cai. O README precisa dizer, com todas as
letras, que a PoC deste marco é a API. Ver T28.

**Segunda consequência:** o gate não pode depender de Docker. Ver D6.

### D4 — Cliente HTML mínimo na 1ª entrega ⚠ novo

O React fica desconectado da API até a Fase 6 (T37). Sem um cliente, o critério
"a solução executa o fluxo principal proposto" (0,1) e o trecho de execução do
vídeo (parte dos 0,3) dependem de o corretor aceitar o Swagger UI como
demonstração.

A referência resolve isso pelo caminho barato: `src/main/resources/static/crud.html`,
HTML e JavaScript nativos, sem build, sem framework, servido pelo próprio
Spring. É meia hora de trabalho e blinda 0,4 da nota. Entra como T25, na Fase 4.

Isso **não** antecipa D2: o build do Vite continua na Fase 6. São dois clientes
diferentes com propósitos diferentes — um demonstra a API, o outro é o produto.

### D5 — `prazo` e fuso horário ⚠ decisão pendente

`modelagem-banco.md` fixa a invariante "`prazo` é data de calendário, gravada à
meia-noite UTC", e o frontend lê com acessores UTC.

O conversor padrão do Spring Data (`Jsr310Converters.LocalDateToDateConverter`)
faz `source.atStartOfDay(ZoneId.systemDefault())`. Em Maringá isso grava
`03:00Z`, não `00:00Z`. O dia UTC continua correto em UTC-3, então funciona por
sorte; em qualquer fuso positivo o dia lido volta errado, e a invariante escrita
no documento fica falsa no banco de qualquer jeito.

Duas saídas, escolher uma e não deixar as duas coexistindo:

1. `TimeZone.setDefault(TimeZone.getTimeZone("UTC"))` na classe `Application`,
   antes do `SpringApplication.run` — uma linha, e a invariante passa a ser
   verdadeira;
2. corrigir a frase em `modelagem-banco.md` para descrever o que de fato
   acontece.

Recomendação: (1). Custa uma linha e mantém o documento que o professor vai ler.

### D6 — A cobertura não pode depender de Docker

`TarefaApiIT` usa Testcontainers, que exige daemon Docker. Se o professor
corrigir numa máquina sem Docker, `./mvnw verify` falha e os **dois** itens de
teste caem (0,2, um deles eliminatório).

Restrição de projeto: **T17 e T18 sozinhos já precisam passar de 70%.** O teste
de integração é evidência adicional, nunca a perna que sustenta o número. O
README documenta os dois comandos e registra o valor obtido.

---

### D7 — `prioridade` é gravada como slug, via conversor ⚠ novo

Descoberto ao inspecionar no `mongosh` o primeiro documento gravado pela
aplicação: o Spring Data persiste `enum` pelo `Enum.name()`, ou seja `"BAIXA"`.
O `@JsonValue` de T9 age **só** na fronteira HTTP — a resposta JSON vinha
`"baixa"` e o banco guardava `"BAIXA"` ao mesmo tempo.

Isso contradiz `modelagem-banco.md`, que fixa o slug ASCII minúsculo como o
valor do campo, e faria **todo insert falhar** contra o `$jsonSchema` de T23,
pela mesma mecânica do `_class` de T22.

Saída adotada: dois conversores em `configuration/MongoConfiguration`
(`@WritingConverter` e `@ReadingConverter`) registrados via
`MongoCustomConversions`. O enum continua idiomático em Java (`ALTA`, `MEDIA`,
`BAIXA`) e o banco continua com o slug.

Alternativa descartada: nomear as constantes em minúsculas (`alta`, `media`,
`baixa`), que dispensaria conversor e `@JsonValue`, mas viola a convenção de
nomes do Java num critério que avalia orientação a objetos.

**Consequência para T22:** `configuration/MongoConfiguration` já existe. T22
acrescenta o `MappingMongoConverter` com `setTypeMapper` nessa mesma classe, em
vez de criá-la.

**Verificado em 08/09/2026**, contra o MongoDB local: documento gravado com
`prioridade: 'media'`, `prazo: ISODate('2026-09-08T00:00:00.000Z')` (D5 confirmada
em UTC-3) e `_class` ainda presente, como T22 prevê.

---

## Fase 1 — Esqueleto e convenção

- [x] **T1. Criar `backend/` com Spring Initializr.**
      Java 21, Spring Boot 3.5.16, empacotamento Jar, Maven.
      Dependências: `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`,
      `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui:2.9.0`,
      `spring-boot-starter-test`, `testcontainers:junit-jupiter`, `testcontainers:mongodb`.
      **Commitar o Maven Wrapper** (`mvnw`, `mvnw.cmd`, `.mvn/`) — a referência
      faz isso para que ninguém precise instalar Maven.
      *Pronto quando:* `./mvnw clean compile` passa.

- [x] **T2. Definir o package base.**
      Sugestão: `br.com.cesumar.aep`. A referência usa `br.com.munif.cesumar`,
      que é do professor — não copiar.
      *Decisão pequena, mas irreversível na prática: renomear package depois
      toca todos os arquivos.*

- [x] **T3. `application.yml`.**
      URI do Mongo por variável de ambiente com default local, no formato da
      referência:
      `uri: ${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/aep}`
      e `springdoc.swagger-ui.path: /docs`.

- [x] **T4. `.gitignore` do backend** (`target/`).

- [x] **T5. Criar `AGENTS.md` na raiz.** ⏱ **antes de T8**
      Faz o papel do `AGENTS.md` **e** do `HARNESS.md` da referência. Vai na
      raiz, e não no `CLAUDE.md`, porque `AGENTS.md` é lido por praticamente
      toda ferramenta de IA e `CLAUDE.md` só pelo Claude Code — o repositório
      tem dois autores e não uma ferramenta só.
      Conteúdo: objetivo do projeto, stack, package base, o fluxo entre as
      camadas, *Definition of Done*, e links para `docs/padroes.md` (T6),
      `docs/arquitetura.md` (T30) e `docs/checklist-revisao.md` (T32).
      Não duplicar aqui o que está em `padroes.md` — apontar.
      **Por que antes do código:** sem código, não há padrão a imitar — quem
      escrever o primeiro `Controller` inventa um. Depois de T8–T16, a convenção
      se infere do que já está lá; antes, não.

- [x] **T6. `docs/padroes.md` — estrutura de classes e de testes.**
      Substitui `spring-crud` e `testing` do `.agents/skills/` da referência.
      Markdown comum em `docs/`, e não skill, por dois motivos: o layout de
      skill é proprietário de ferramenta (Codex usa `.agents/skills/`, Claude
      Code usa `.claude/skills/`, nenhum serve os dois) e nenhum dos dois é
      visível para o professor lendo o repositório no GitHub.
      Define package base, nomes por camada, ordem dos membros na classe,
      responsabilidade e proibições de cada camada; e, para teste, os três tipos,
      a regra de sufixo `*Test`/`*IT`, nomenclatura, matriz mínima por domínio e
      a restrição de cobertura de D6.

- [x] **T7. Enxugar o `CLAUDE.md` e apontá-lo para o `AGENTS.md`.**
      As convenções de commit, branches, "não commitar sem pedido" e
      "comentários sem marca de ferramenta" que já existem lá valem para
      qualquer ferramenta — **movem** para o `AGENTS.md`. O `CLAUDE.md` fica
      curto, com o que é específico do Claude Code, e com uma instrução para ler
      o `AGENTS.md`.
      Uma regra em dois arquivos envelhece em um deles. Mover, não copiar.
      Acrescentar `backend` à lista de escopos de commit na mudança.

---

## Fase 2 — Domínio CRUD

Ordem de implementação da skill `spring-crud` da referência, agora registrada em
`docs/padroes.md`:
`Model → Repository → Request DTOs → Response DTOs → Mapper → Service → Controller → Exceptions → Tests`.

- [x] **T8. `model/Tarefa`.**
      `@Document(collection = "tarefas")`, `@Id String id`, campos de D1.
      Construtor + getters/setters escritos à mão — a referência proíbe Lombok
      explicitamente (regra 20 do `AGENTS.md`).
      Nunca atravessa a fronteira HTTP.
      `prazo` como `LocalDate`; ver D5 antes de escrever a classe.

- [x] **T9. `model/Prioridade` como `enum`** (`ALTA`, `MEDIA`, `BAIXA`).
      A referência usa String pura, mas "aplicação efetiva do paradigma
      orientado a objetos" é critério de nota — e o enum entrega validação de
      graça: valor inválido no JSON vira `HttpMessageNotReadableException`,
      já tratada pelo handler de T16.
      Serializar em minúsculas sem acento (`alta`/`media`/`baixa`) para o
      contrato HTTP, conforme já decidido em `modelagem-banco.md`.

- [x] **T10. `repository/TarefaRepository extends MongoRepository<Tarefa, String>`.**
      Vazio. Só persistência, nenhuma regra.

- [x] **T11. DTOs como `record`, um por caso de uso.**
      A referência é explícita: não reutilizar DTO entre criação, atualização e
      resposta só porque os campos coincidem hoje.
      - `TarefaCreateRequest` — `titulo`, `prazo`, `prioridade`, `dono`.
        **Sem `id` e sem `concluida`:** tarefa nasce pendente, é regra de
        negócio, não entrada do cliente.
      - `TarefaUpdateRequest` — `titulo`, `prazo`, `prioridade`, `concluida`.
        Sem `id`: ele vem do path.
      - `TarefaResponse` — representação completa.
      - `TarefaSummaryResponse` — `id`, `titulo`, `prazo`, `concluida`.
        A projeção da listagem, como `LinguagemSummaryResponse` na referência.
      Jakarta Validation nos Requests: `@NotBlank`, `@NotNull`, mensagens em
      português.

- [x] **T12. `mapper/TarefaMapper`** (`@Component`).
      `toModel`, `updateModel`, `toResponse`, `toSummaryResponse`. Java
      explícito, sem MapStruct. Não acessa Repository nem tem regra de negócio.
      `updateModel` **preserva o id persistido**.
      **Fronteira com T13:** o Mapper traduz apenas campos vindos do cliente.
      `toModel` deixa `concluida`, `criadaEm` e `atualizadaEm` como estão — quem
      os define é o Service. Sem essa linha, duas pessoas implementam de dois
      jeitos, que é exatamente o que T5 e T6 existem para evitar.

- [x] **T13. `service/TarefaService`.**
      Injeção por construtor. Casos de uso: `listar`, `buscarPorId`, `criar`,
      `atualizar`, `excluir`.
      Regras que vivem aqui, não no Controller nem no Mapper:
      - criação define `concluida = false`, `criadaEm = atualizadaEm = agora`;
      - atualização mexe em `atualizadaEm`;
      - ausência vira `TarefaNotFoundException`.

      ⚠ **Decisão pendente — como o Service obtém "agora":**
      `Instant.now()` inline custa zero linha, mas o teste de T17 só consegue
      afirmar "não é nulo" e "é ≥ o instante anterior à chamada"; e o teste de
      "atualizar mexe em `atualizadaEm`" fica potencialmente instável, porque
      duas chamadas seguidas a `Instant.now()` podem devolver o mesmo valor no
      Windows. `Clock` injetado no construtor custa três linhas e um bean, e
      torna a data verificável exatamente. Decidir antes de escrever T17.

- [x] **T14. `controller/TarefaController`** em `/api/tarefas`.

      | Método | Rota | Sucesso |
      |---|---|---|
      | `GET` | `/api/tarefas` | `200`, lista resumida |
      | `GET` | `/api/tarefas/{id}` | `200`, representação completa |
      | `POST` | `/api/tarefas` | `201` + header `Location` + corpo completo |
      | `PUT` | `/api/tarefas/{id}` | `200`, corpo completo |
      | `DELETE` | `/api/tarefas/{id}` | `204`, sem corpo |

      `@Valid` nos corpos. Nenhum `try/catch` para fluxo HTTP normal.

- [x] **T15. `exception/TarefaNotFoundException`** + `exception/ApiError`
      (`record` com `status`, `error`, `message`, `path`, `fieldErrors`).

- [x] **T16. `exception/GlobalExceptionHandler`** (`@RestControllerAdvice`).
      Trata `TarefaNotFoundException` → `404`,
      `MethodArgumentNotValidException` → `400` com o mapa `fieldErrors`,
      `HttpMessageNotReadableException` → `400` (cobre JSON malformado e
      prioridade inválida do enum de T9).

---

## Fase 3 — Testes e cobertura

Sem esta fase o item "Cobertura ≥ 70%" recebe **0 ponto**, e ele é eliminatório
por si só no enunciado. A matriz completa de casos está em `docs/padroes.md`.

- [x] **T17. `TarefaServiceTest`** — JUnit 5 + Mockito, sem Spring, sem Mongo.
      Casos: listar mapeia para resumo; buscar existente; buscar inexistente
      lança; criar força `concluida = false` e carimba as datas; atualizar
      preserva o id e mexe em `atualizadaEm`; excluir inexistente lança e não
      chama `delete`.

- [x] **T18. `TarefaControllerTest`** — `@WebMvcTest` + MockMvc, Service mockado.
      Casos: `200` na listagem devolvendo **só** os campos do resumo — com
      `jsonPath(...).doesNotExist()` nos demais, senão a projeção não está
      testada; `200` na busca completa; `201` com `Location`; `400` com
      `fieldErrors` para corpo inválido; `404`; `204` no delete.
      Não repetir aqui regra já coberta em T17.
      *Pronto quando:* `./mvnw clean test` passa **e** T17 + T18 sozinhos já
      passam de 70% no relatório (D6).

<!-- adiado em 09/09/2026: exige daemon Docker, desligado na máquina onde o
     backend foi escrito. O failsafe já está registrado (T20), então basta o
     arquivo existir para ele rodar no verify de quem tiver Docker. -->
- [ ] **T19. `TarefaApiIT`** — `@SpringBootTest` + Testcontainers com a **mesma
      imagem do Compose** (ver T21). Fluxo HTTP completo: criar, buscar, listar,
      atualizar, excluir. `repository.deleteAll()` no `@BeforeEach`.
      Não depende do Mongo instalado, do Compose nem da ordem de execução.
      **O que ele não prova:** o contêiner sobe um Mongo limpo, sem executar
      `banco/init.js` — logo sem `$jsonSchema` e sem índices. A conformidade com
      o validador do banco de desenvolvimento é verificada em T24, à mão.

- [x] **T20. JaCoCo + Failsafe no `pom.xml`.**
      `prepare-agent`, `report` e `check` na fase `verify`, com limite
      `LINE / COVEREDRATIO / 0.70`. Excluir a classe `Application` e o package
      de configuração, como a referência faz.
      Failsafe registrado para o `*IT` rodar no `verify`.
      *Pronto quando:* `./mvnw clean verify` passa **e** o número de
      `target/site/jacoco/index.html` está anotado no README.
      **Feito em 09/09/2026:** `verify` verde, **96,7% de linha** (117/121) só
      com T17 + T18, sem Docker. Anotar o número no README é parte de T28, que
      ainda reescreve o arquivo inteiro.

---

## Fase 4 — Banco, infraestrutura e demonstração

- [x] **T21. Trocar `mongo:8` por `mongo:7.0` no `docker-compose.yml`.**
      A referência documenta que a imagem `mongo:8.0` **recusa iniciar** em
      hosts com kernel Linux 6.19 ou mais novo, e por isso fixou 7.0.
      Se o professor corrigir em Linux, nosso Compose atual não sobe na máquina
      dele. Alinhar Compose e Testcontainers (T19) na mesma versão.
      De quebra, some a tag flutuante: `mongo:8` muda de conteúdo sem aviso.

- [x] **T22. Desligar o campo `_class` do Spring Data.** ⚠ **bloqueia T23 e T24**
      O `MappingMongoConverter` grava por padrão um campo `_class` com o nome da
      classe Java. Com `additionalProperties: false`, isso conta como campo
      extra e **todo insert falha** com `Document failed validation` — a mesma
      armadilha que o `_id` já causou e que está documentada em
      `modelagem-banco.md`.
      Duas saídas; a segunda é a correta:
      1. declarar `_class` em `properties` — suja o schema com detalhe de
         implementação Java;
      2. em `configuration/MongoConfiguration`, registrar um
         `MappingMongoConverter` com
         `setTypeMapper(new DefaultMongoTypeMapper(null))`.
      *Pronto quando:* um documento gravado pela aplicação e lido no `mongosh`
      não tem `_class`.
      **Feito em 09/09/2026:** saída adotada (2). Verificado no `mongosh` contra
      o mongod local: o documento gravado pela API não tem `_class`, `prioridade`
      vem como slug e `prazo` vem em `00:00:00.000Z`.

      **Não bloqueia T19.** O Testcontainers sobe um Mongo limpo, sem o
      validador do `init.js`; o `_class` passa despercebido lá. A falha aparece
      só contra o Compose — ou seja, na hora de gravar o vídeo, com a suíte
      verde. É por isso que T24 existe.

- [x] **T23. Reescrever `banco/init.js` para a coleção `tarefas`.**
      `$jsonSchema` fechado (`additionalProperties: false`) nos moldes do atual,
      `enum` em `prioridade`, índice em `{ dono: 1 }` e seed de demonstração.
      Manter o comentário sobre `docker compose down -v`.
      **Feito em 09/09/2026.** O schema foi aplicado à mão a um mongod local e
      `POST` e `PUT` pela API passaram por ele. Contra o Compose continua sendo
      T24.

- [ ] **T24. Smoke test manual contra o Compose.** ⚠ **antes de T33**
      A suíte automatizada não cobre o validador do banco (ver T19/T22). Uma vez,
      à mão: `docker compose down -v && docker compose up -d`, subir a aplicação,
      criar e atualizar uma tarefa pelo Swagger em `/docs`, e conferir no
      `mongosh` que o documento gravado não tem `_class` e passa pelo
      `$jsonSchema`.
      Registrar o resultado no `docs/decisoes.md` ou no README — é a evidência
      de que a PoC roda de verdade, não só nos testes.

- [ ] **T25. `src/main/resources/static/crud.html`** (D4).
      HTML, CSS e JavaScript nativos, sem build e sem framework, servido pelo
      próprio Spring: listar, criar, atualizar e excluir tarefas contra
      `/api/tarefas`. Cópia estrutural do `crud.html` da referência.
      É o que transforma "a API responde no Swagger" em "a PoC executa o fluxo
      principal" — o critério de 0,1 e o trecho de execução do vídeo.
      Não confundir com D2: o build do Vite continua na Fase 6 (T36).

---

## Fase 5 — Documentação e entrega

- [ ] **T26. Definir o ODS e o problema.** ⚠ **bloqueia T28 e o vídeo**
      Vale 0,1 e é pré-requisito do README e do roteiro. A rubrica pede três
      coisas, não uma: problema claramente definido, **público/contexto
      identificável** e **relação objetiva** com o ODS escolhido — não basta
      citar o número do objetivo. Já está listado como "Em aberto" em
      `modelagem-banco.md`. Decisão do grupo — ver `sugestoes-projetos.md`.

- [x] **T27. Atualizar `docs/modelagem-banco.md` para D1.**
      O documento hoje especifica o agregado `usuarios`. Ele passa a
      especificar a coleção `tarefas` achatada, e o agregado vira a seção
      "Caminho para a 2ª entrega". A análise de campos derivados continua
      válida. A análise de datas/fuso precisa ficar coerente com a saída
      escolhida em D5.

- [ ] **T28. Reescrever o `README.md` da raiz.** Hoje tem uma linha.
      É critério de nota direto ("README inicial com problema, ODS, tecnologias
      e instruções básicas de execução"). Precisa de: problema, ODS, público,
      stack, como subir o banco, como executar, como rodar os testes, como gerar
      o relatório de cobertura e o número obtido.
      Dois itens que não são óbvios e valem ponto:
      - **delimitar o escopo da PoC deste marco** (D3): a PoC medida é a API; o
        cliente React está em desenvolvimento e não faz parte da medição;
      - **os dois comandos de teste** (D6): `./mvnw clean test` roda sem Docker,
        `./mvnw clean verify` inclui a integração e o gate de cobertura.

- [ ] **T29. `docs/decisoes.md` com as ADRs do projeto.**
      A referência tem 12. As nossas mínimas: Java 21 + Spring Boot; MongoDB;
      camadas Controller/Service/Repository; DTOs por operação; mapping
      explícito; Testcontainers; **achatamento da 1ª entrega (D1)**;
      **empacotamento do frontend dentro do Spring (D2)**; **cliente HTML mínimo
      na 1ª entrega (D4)**; **fuso do `prazo` (D5)**; versão 7.0 do Mongo.

- [ ] **T30. `docs/arquitetura.md`.** ← rende nota
      Fluxo de entrada e de saída entre as camadas e a responsabilidade de cada
      uma, nos moldes do `architecture.md` da referência. É o documento que
      sustenta o critério "responsabilidades compreensíveis" e o trecho de
      arquitetura do vídeo. Uma página; mais que isso não é lido.
      Não repetir `docs/padroes.md`: aqui é o desenho, lá é a regra de escrita.

- [ ] **T31. `docs/api-http.md`.** ← rende nota, quase de graça
      Tabela de contratos (método, caminho, entrada, sucesso, recurso ausente),
      exemplos de corpo, regras de validação e o formato do erro.
      O Springdoc já expõe o contrato vivo em `/docs`; este arquivo existe para
      quem lê o repositório sem subir a aplicação — que é exatamente a situação
      do professor corrigindo.

- [ ] **T32. `docs/checklist-revisao.md`.**
      Adaptar o `.agents/skills/architecture-review/SKILL.md` da referência e
      linkar do `AGENTS.md`. Rodar uma vez antes da tag de T33.
      Ele é, linha por linha, o que o professor procura no diff: Controller sem
      regra de negócio, Model exposto, DTO genérico por conveniência, Repository
      com lógica, ID perdido no update, exceção não tratada, teste dependente de
      ordem, documentação fora de sincronia.
      Complementa `docs/padroes.md`: aquele é lido antes de escrever, este antes
      de abrir PR.
      *Se* um dia quisermos ergonomia de slash-command, um wrapper fino em
      `.claude/skills/` aponta para este mesmo arquivo (hoje o `.gitignore` só
      exclui `.claude/settings.local.json`, então o resto já seria versionado).

- [ ] **T33. Fechar a 1ª entrega: merge na `main` + tag.**
      Requisito explícito da seção 9 do enunciado: "identificação clara da
      versão correspondente a cada entrega".
      A tag sai da `main`, **depois** de mesclar `samuel` e `felipe`. Sem isso o
      histórico da branch entregue mostra um autor só, e "histórico de commits"
      é critério de nota (0,1).

- [ ] **T34. Slides para o vídeo — opcional, mas o vídeo vale 0,3.**
      A referência tem `docs/apresentacao.html`, 75 KB autocontidos, sem
      dependência. Não pontua por si, mas o vídeo é o item mais caro da entrega
      e precisa caber em 2 a 3 minutos cobrindo problema, ODS, arquitetura e
      execução. Slide pronto é o que impede a gravação de virar improviso de
      cinco minutos.
      Só fazer depois de T26 e T28; antes disso não há conteúdo para colocar.

- [ ] **T35. Gravar o vídeo de 2 a 3 minutos.**
      Vale 0,3 — é o item mais caro da entrega inteira, mais que o dobro de
      qualquer outro. Precisa de: problema, ODS, projeto inicial e a PoC
      executando — pelo `crud.html` de T25, não pelo Swagger.

---

## Fase 6 — Depois da 1ª entrega

- [ ] **T36. Build do Vite para `src/main/resources/static/`** (D2).

- [ ] **T37. Trocar os mocks do frontend por `fetch`** — `services/auth.ts` e os
      dados de `Inicio`/`Anotacoes`. Ver seções A e B do `frontend/TODO.md`,
      que já mapeiam item por item o que morre quando a API existir.

- [ ] **T38. Autenticação real e a modelagem da 2ª entrega:** `usuarios` como
      coleção própria, `dono` vira `usuarioId`, `categorias` com anotações
      aninhadas.

- [ ] **T39. Quadro de tarefas.** Critério de nota da **2ª** entrega
      ("Metodologia de trabalho utilizada", 0,1). Não pontua agora, mas o
      histórico precisa existir desde já para ser demonstrável depois.

---

## Caminho crítico

Restava um bloqueio real: **T26** (ODS, trava README e vídeo). **T22**
(`_class`) e **T5/T6** (convenção) já saíram.

```
T1 → T5 → T6 → T8..T16 → T17..T20 → T22 → T23 → T24 → T25 → T28 → T33 → T35
          ↑                              ↑                  ↑
     convenção antes               não bloqueia         T26 (ODS)
       do código                      T19
```

Os itens que mais rendem por hora de trabalho, em ordem: **T25** (cliente HTML,
sustenta 0,1 do fluxo principal e boa parte dos 0,3 do vídeo), **T28** (README,
0,1 direto e ancora outros dois critérios) e **T18** (o teste de contrato é o
que faz a cobertura fechar sem depender de Docker).
