# Decisões de arquitetura (ADRs)

Registro das decisões técnicas do projeto: o que foi decidido, contra o que, e o
que a escolha custou. Uma decisão sem alternativa descartada não é decisão, é
acidente — por isso cada bloco tem a alternativa que caiu.

Ordem cronológica de adoção. O planejamento por tarefa está em
`docs/backend-tarefas.md`; a especificação do banco em `docs/modelagem-banco.md`.

---

## ADR-01 — Java 21 com Spring Boot 3.5.16

**Status:** aceito

**Contexto.** O enunciado exige "linguagem com suporte a programação orientada a
objetos e aplicação efetiva do paradigma". O projeto de referência do professor
usa Java 21 com Spring Boot.

**Decisão.** Java 21 (LTS) e Spring Boot 3.5.16, com o wrapper do Maven
commitado.

**Consequências.** O wrapper commitado significa que quem corrige não precisa ter
Maven instalado — `./mvnw` basta. A máquina de desenvolvimento tem JDK 25, e o
build compila com `release 21` de propósito: o alvo é a versão declarada, não a
do JDK presente.

**Alternativa descartada.** Node com TypeScript aproveitaria a stack do
frontend, mas afasta da referência de correção e enfraquece o critério de POO.

---

## ADR-02 — MongoDB como banco NoSQL

**Status:** aceito

**Contexto.** Uma das quatro disciplinas é Banco de Dados NoSQL, e o uso efetivo
de NoSQL é requisito obrigatório.

**Decisão.** MongoDB, via Spring Data MongoDB, subido por `docker-compose.yml`.

**Consequências.** O `$jsonSchema` dá validação no banco, e não só na aplicação
— o que vira evidência de "uso efetivo". Em troca, exige lidar com as
divergências entre o que o Spring Data grava e o que o schema aceita (ADR-07 e
ADR-08).

---

## ADR-03 — Corte por camada, não por funcionalidade

**Status:** aceito

**Contexto.** O critério de nota é "responsabilidades compreensíveis".

**Decisão.** Pacotes por responsabilidade — `model`, `repository`, `dto`,
`mapper`, `service`, `controller`, `exception`, `configuration` — e não por
funcionalidade (`tarefa/`, `usuario/`).

**Consequências.** A responsabilidade fica visível na própria árvore de
diretórios, sem precisar abrir arquivo. O desenho está em
`docs/arquitetura.md`.

**Alternativa descartada.** Pacote por funcionalidade escala melhor em sistema
grande, mas aqui há um domínio só — criaria oito pastas de um arquivo cada e
esconderia exatamente o que o avaliador quer ver.

---

## ADR-04 — Um DTO por caso de uso

**Status:** aceito

**Contexto.** Quatro operações com necessidades diferentes de entrada e saída.

**Decisão.** Quatro `record`s: `TarefaCreateRequest`, `TarefaUpdateRequest`,
`TarefaResponse`, `TarefaSummaryResponse`. O `@Document` nunca atravessa a
fronteira HTTP.

**Consequências.** Duas regras de negócio passam a ser garantidas pelo tipo, não
por código defensivo: `concluida` não existe no DTO de criação (tarefa nasce
pendente) e `criadaEm` não existe no de atualização (o `PUT` não pode apagá-lo
por omissão). A projeção da listagem vira decisão explícita.

Custo real: quatro arquivos e um mapper para uma entidade de oito campos. Aceito
— é o que o critério pede.

**Alternativa descartada.** Um DTO único reaproveitado nas quatro operações.
Obrigaria a validar por operação dentro do Service e abriria a porta para o
cliente enviar `concluida: true` na criação.

---

## ADR-05 — Mapping explícito, sem Lombok e sem MapStruct

**Status:** aceito

**Contexto.** O critério avalia uso coerente de classes e objetos.

**Decisão.** `TarefaMapper` escrito à mão. Getters e setters escritos. Nenhum
gerador de código.

**Consequências.** Mais linhas, e todas lidas. O que o avaliador abre é o código
que existe, não bytecode gerado em tempo de build — e a fronteira "o Mapper só
traduz o que veio do cliente; o derivado nasce no Service" fica verificável numa
leitura.

**Alternativa descartada.** Lombok e MapStruct encurtariam o arquivo, mas
esconderiam justamente o que está sendo avaliado.

---

## ADR-06 — Coleção única `tarefas`, com documento achatado

**Status:** aceito · substitui a modelagem anterior, de 09/09/2026

**Contexto.** A seção 8 do enunciado limita a 1ª entrega a uma coleção, objetos
homogêneos com estrutura simples e CRUD básico. As telas têm quatro entidades
naturais: usuário, tarefa, categoria e anotação.

**Decisão.** Coleção única `tarefas`, documento achatado, com `observacao` como
campo escalar de texto livre. Nenhum array de subdocumentos.

**Consequências.** **Login e cadastro continuam mock nesta entrega** — não há
`senhaHash` porque não há coleção de usuários. Em troca, a evolução da 2ª entrega
fica aditiva e visível em diff: `usuarioId` entra na tarefa, e `categorias` entra
com as anotações aninhadas.

**Alternativa descartada.** Coleção `usuarios` com tarefas aninhadas persistiria
o login já agora, mas array de subdocumentos é literalmente o exemplo que a
seção 8 usa para descrever a **2ª** entrega. Gastaria o critério "evolução
verificável". As demais alternativas estão tabeladas em
`docs/modelagem-banco.md`.

---

## ADR-07 — `prioridade` é gravada como slug, via conversor

**Status:** aceito

**Contexto.** O `enum` Java é `ALTA`/`MEDIA`/`BAIXA`; o `$jsonSchema` aceita
`"alta"`/`"media"`/`"baixa"`. O Spring Data persiste `enum` por `Enum.name()`,
logo gravaria `"BAIXA"` e falharia na validação. O `@JsonValue` resolve só a
fronteira HTTP — não a do banco.

**Decisão.** Um par de conversores (`@WritingConverter`/`@ReadingConverter`) em
`MongoConfiguration`. O Java segue com as constantes, o banco com o slug ASCII.

**Consequências.** Acento nunca entra no dado; o rótulo acentuado é da tela.
Slug desconhecido estoura na desserialização, antes do Bean Validation, e chega
como `400` com `fieldErrors` vazio — é por isso que o DTO não tem validador de
prioridade.

---

## ADR-08 — `$jsonSchema` fechado e campo `_class` desligado

**Status:** aceito

**Contexto.** Por padrão o `$jsonSchema` aceita campo não declarado. Um
`updateOne` com o nome errado **cria** o campo, calado, e o documento passa a
divergir dos outros — aconteceu durante a montagem, com um `atualizadoEm` que
nasceu na raiz.

**Decisão.** `additionalProperties: false` no validador, e
`DefaultMongoTypeMapper(null)` para o Spring Data não gravar `_class`.

**Consequências.** Typo de campo vira `Document failed validation` na hora da
escrita, em vez de divergência silenciosa. Duas exigências que surpreendem vêm
junto: `_id` precisa estar declarado em `properties`, e o bean
`MappingMongoConverter` tem de ser redefinido inteiro (o Boot só o cria quando
não existe outro), o que obriga a repetir a fiação dele.

Efeito colateral cosmético, verificado em 11/09/2026: o MongoDB registra no
startup o aviso *"$jsonSchema validator does not allow '_id' field"*. É falso
positivo da heurística dele — ela alerta para qualquer schema fechado com
`required`, porque um documento contendo só `_id` seria recusado. O insert real
é aceito; a evidência está no fim deste arquivo.

---

## ADR-09 — O fuso do processo é fixado em UTC

**Status:** aceito · resolve a decisão pendente D5 de `backend-tarefas.md`

**Contexto.** `prazo` é data de calendário, e a modelagem fixa a invariante
"gravada à meia-noite UTC". Mas o conversor padrão do Spring Data faz
`source.atStartOfDay(ZoneId.systemDefault())`: em Maringá gravaria `03:00Z`, e em
qualquer fuso positivo gravaria o dia **seguinte**. A invariante escrita no
documento ficaria falsa no banco.

**Decisão.** `TimeZone.setDefault(TimeZone.getTimeZone("UTC"))` em
`AepApplication`, antes do `SpringApplication.run`.

**Consequências.** Uma linha, e a invariante passa a valer em qualquer máquina —
inclusive na de quem corrige. Como `@SpringBootTest` não passa pelo `main`, o
teste de integração **não** herda o fuso fixado: ele prova o CRUD, não a
invariante. Essa é verificada à mão (ver evidência no fim).

**Alternativa descartada.** Corrigir a frase em `modelagem-banco.md` para
descrever o que de fato acontecia. Mais honesto que nada, mas deixaria o dado
dependente da máquina.

---

## ADR-10 — O relógio é injetado

**Status:** aceito

**Contexto.** `criadaEm` e `atualizadaEm` nascem no Service. Com
`Instant.now()` chamado direto, o teste só conseguiria afirmar "não é nulo".

**Decisão.** Um bean `Clock` em `ClockConfiguration`, injetado no Service.

**Consequências.** O teste afirma valor exato com `Clock.fixed`. Resolve também
um problema achado na prática: no Windows, duas chamadas seguidas a
`Instant.now()` podem devolver o mesmo valor, o que tornaria instável qualquer
teste que comparasse `atualizadaEm` com `criadaEm`.

---

## ADR-11 — MongoDB fixado em 7.0

**Status:** aceito

**Contexto.** O Compose usava `mongo:8`, tag flutuante.

**Decisão.** `mongo:7.0`, e a **mesma** imagem no Testcontainers.

**Consequências.** Tag flutuante muda de conteúdo sem aviso, e a 8.0 recusa
iniciar em hosts com kernel Linux 6.19 ou mais novo — quebraria o Compose
justamente na máquina de quem corrige. Usar a mesma imagem nos dois lugares
impede que o teste passe contra uma versão que o projeto não usa.

---

## ADR-12 — A cobertura não pode depender de Docker

**Status:** aceito

**Contexto.** `TarefaApiIT` usa Testcontainers, que exige daemon Docker. Se quem
corrige não tiver Docker, `./mvnw verify` falharia e os **dois** itens de teste
cairiam — 0,2, um deles eliminatório.

**Decisão.** Restrição de projeto: `TarefaServiceTest` e `TarefaControllerTest`
**sozinhos** precisam passar de 70%. O `*IT` é evidência adicional, nunca a perna
que sustenta o número. O sufixo decide o executor: `*Test` no surefire
(`test`), `*IT` no failsafe (`verify`).

**Consequências.** Dois comandos documentados no README, com os dois números
medidos — ver a tabela em "Verificações registradas" no fim deste arquivo para
o valor atual. O gate do JaCoCo está em 70% de linha, excluindo
`AepApplication` e o pacote `configuration`.

---

## ADR-13 — A cobertura é medida só no backend

**Status:** aceito

**Contexto.** O enunciado pede 70% "sobre o código da PoC entregue". O
repositório tem backend e um frontend React que ainda consome mocks.

**Decisão.** A PoC medida nesta entrega é a **API**. O React está em
desenvolvimento e é declarado fora da medição, explicitamente no README.

**Consequências.** Delimitar o escopo é o que evita o avaliador calcular
cobertura sobre o repositório inteiro e achar um número baixo. Exige que o README
diga isso em voz alta, não por omissão.

---

## ADR-14 — Cliente HTML mínimo na 1ª entrega

**Status:** aceito

**Contexto.** O critério "primeira versão funcional da PoC" (0,1) pede que a
solução execute o fluxo principal. O Swagger prova que a API responde, não que a
PoC executa. E o React não fala com a API.

**Decisão.** `src/main/resources/static/crud.html` — HTML, CSS e JavaScript
nativos, sem build e sem framework, servido pelo próprio Spring.

**Consequências.** O fluxo principal fica demonstrável em vídeo sem depender do
frontend. Mesma origem, logo sem CORS e sem proxy. Duas decisões internas dele
merecem registro: o formulário usa `novalidate` de propósito, para que a
validação exercitada seja a **da API** e o `400` com `fieldErrors` apareça; e
`prazo` é formatado por manipulação de string, nunca por `new Date()`, que
erraria o dia em fuso negativo.

**Alternativa descartada.** Demonstrar pelo Swagger. Mais barato, mas "executa o
fluxo principal" lido com rigor não é uma tela de documentação.

---

## ADR-15 — O React será empacotado dentro do Spring

**Status:** aceito, implementação adiada para a 2ª entrega

**Contexto.** Frontend e backend em processos separados exigiriam CORS ou proxy,
e duas coisas para subir na demonstração.

**Decisão.** O build do Vite passa a sair em `src/main/resources/static/`. Um
artefato, uma porta, uma origem.

**Consequências.** Fica para a 2ª entrega (T36), junto com a troca dos mocks por
`fetch`. Nesta entrega o React roda no Vite e não faz parte da PoC medida
(ADR-13). Não confundir com ADR-14: o `crud.html` é outro cliente, e continua
existindo depois.

---

## ADR-16 — ODS 4, com o problema recortado em organização de estudos

**Status:** aceito em 11/09/2026

**Contexto.** O critério "Problema e alinhamento ao ODS" (0,1) exige três coisas,
não uma: problema claramente definido, público identificável e relação objetiva
com o ODS. O que está construído é um gerenciador de tarefas, então o
enquadramento precisa caber no domínio sem reescrevê-lo.

**Decisão.** **ODS 4 — Educação de Qualidade.** Problema: o estudante que
trabalha perde prazos e perde o material de estudo por não ter um lugar único —
a agenda vive espalhada entre aplicativo de mensagens, papel e memória, e a
desorganização, não a dificuldade com o conteúdo, é o que derruba a disciplina.
Público: estudante trabalhador do noturno, do técnico e da graduação.

**Consequências.** O problema se divide exatamente nas duas entregas, o que
transforma "evolução verificável" em consequência do recorte em vez de promessa:

| Metade do problema | Entrega | Modelagem |
|---|---|---|
| tarefas pendentes com prazo | 1ª | coleção `tarefas` achatada (ADR-06) |
| materiais de estudo organizados | 2ª | `categorias` com anotações aninhadas |

**Alternativa descartada.** Adotar uma das sugestões de `sugestoes-projetos.md`
como escritas (ODS 2, 11 ou 12). Problema mais imediatamente reconhecível, mas
custaria reescrever modelo, `init.js`, modelagem, os dois testes e as telas — a
dias da entrega, trocando trabalho verde por risco.

---

## ADR-17 — `dono` dá lugar a `observacao`

**Status:** aceito em 11/09/2026 · altera ADR-06

**Contexto.** O documento tinha `dono`, uma string livre que identificava de quem
era a tarefa enquanto não houvesse coleção de usuários. Na prática ela não
identificava ninguém: sem autenticação, nada garantia que o valor enviado
correspondia a quem chamava a API, e a tela pedia o nome do responsável como
texto digitado.

**Decisão.** O campo passa a ser `observacao`: texto livre **opcional**, com um
detalhe sobre a tarefa. Renomeado no Model, nos quatro DTOs, no Mapper, no
`$jsonSchema` e no seed. Entra também no resumo da listagem, para a tela não
precisar de um segundo request para exibi-lo.

O índice `{ dono: 1 }` foi removido junto: existia para sustentar a listagem por
dono, e sem o campo não há consulta que o use.

**Consequências.**

- A tarefa passa a **não ter nenhum campo de usuário** nesta entrega. Isso é
  coerente com a autenticação mock, e está declarado nos limites do README, de
  `docs/api-http.md` e de `docs/arquitetura.md`.
- **O caminho da 2ª entrega muda de forma.** Era "`dono` vira `usuarioId`" — uma
  renomeação, que mostrava a evolução como um diff mínimo. Agora é "`usuarioId`
  entra", uma adição. Continua aditivo e continua atendendo o critério "evolução
  verificável", mas perde a leitura de que o campo já antecipava o
  relacionamento.
- `observacao` é **opcional** (revisto em 11/09/2026 — ver ADR-18). A primeira
  versão a tornava obrigatória; forçar o usuário a escrever algo em toda tarefa
  não tinha justificativa de domínio, e o texto livre não é um dado que a PoC
  precise garantir presente.

**Alternativa descartada.** Manter `dono` e adicionar `observacao` como campo
novo. Daria as duas coisas, mas acrescentaria um campo que ninguém preenche com
sentido e mantém a falsa promessa de identificar o responsável.

---

## ADR-18 — `observacao` é opcional, não obrigatória

**Status:** aceito em 11/09/2026 · corrige ADR-17

**Contexto.** A primeira versão de ADR-17 marcou `observacao` como obrigatória
(`@NotBlank` nos dois DTOs, listada em `required` no `$jsonSchema`). Revisando a
decisão: uma observação de texto livre que o usuário é forçado a preencher em
toda tarefa não tem justificativa de domínio — ao contrário de `titulo`,
`prazo` e `prioridade`, que a tarefa não faz sentido sem.

**Decisão.** `observacao` vira opcional. Removido o `@NotBlank` de
`TarefaCreateRequest` e `TarefaUpdateRequest`; removido de `required` no
`$jsonSchema`.

**Consequências.**

- Verificado empiricamente: quando o campo não vem no corpo da requisição, o
  Model chega ao Service com `observacao == null`, e o Spring Data **omite a
  chave por completo** na escrita — não grava `observacao: null`. A resposta
  HTTP, por sua vez, mostra `"observacao": null` normalmente, porque o
  serializador do Controller não omite campo nulo.
- O `$jsonSchema` não precisou aceitar `null` como tipo (`["string", "null"]`):
  como o Spring Data nunca escreve a chave quando o valor é nulo, bastou tirá-la
  de `required` e manter `bsonType: "string"` — ela só é validada quando
  presente.
- `TarefaSummaryResponse` e `TarefaResponse` continuam com o campo sempre
  presente na saída JSON, valendo `null` quando a tarefa não tem observação.
  Nenhum cliente precisa checar `hasOwnProperty` — o campo sempre existe na
  resposta, só o valor muda.

**Alternativa descartada.** Manter obrigatória e aceitar o custo de forçar
preenchimento. Rejeitada por não ter regra de negócio por trás — seria
obrigatoriedade por semelhança com os outros campos, não por necessidade.

---

## Verificações registradas

Evidência das checagens que a suíte automatizada não cobre. Reproduzíveis pelos
comandos indicados.

### Conformidade com o banco de desenvolvimento — 11/09/2026

> Refeita duas vezes no mesmo dia: primeiro após a renomeação `dono` →
> `observacao` (ADR-17), depois após `observacao` virar opcional (ADR-18). A
> tabela abaixo é a segunda medição; as anteriores não valem mais.

Exigida porque o Testcontainers sobe um Mongo **limpo**: ele não executa
`banco/init.js`, logo não tem o `$jsonSchema` nem os índices. O que o teste de
integração prova é o CRUD; a conformidade com o validador é esta verificação.

Feita contra o Compose (`docker compose down -v && docker compose up -d`), com a
aplicação subida por `./mvnw spring-boot:run`, criando e atualizando tarefas pela
API e inspecionando o documento cru no `mongosh`:

| Verificação | Resultado |
|---|---|
| `banco/init.js` executado pelo entrypoint da imagem | coleção `tarefas`, `observacao` **fora** de `required` e 3 documentos do seed |
| `_id` declarado no `$jsonSchema` | presente; insert válido aceito |
| documento gravado pela API tem `_class` | **não** |
| `prioridade` gravada como slug | sim (`alta`, `baixa`) |
| `POST` sem `observacao` | `201`; a chave **some** do documento gravado (Spring Data omite campo nulo) |
| `POST` sem `titulo` | `400` com `fieldErrors.titulo` — os demais campos seguem obrigatórios |
| nenhum índice além de `_id_` | confirmado |
| `prazo` gravado à meia-noite UTC | sim — `2026-09-25T00:00:00.000Z` |
| documento com todos os campos tem 8 chaves; sem `observacao`, 7 | sim |
| documento com campo não declarado | recusado pelo validador |
| `POST` → `201` com `Location`; `PUT` → `200` alterando `observacao` | sim |
| corpo vazio → `400` com `fieldErrors` por campo | sim |
| id inexistente → `404` com `fieldErrors` vazio | sim |

**Desvio de ambiente, declarado.** A máquina onde a verificação rodou tem o
serviço MongoDB do Windows ocupando `127.0.0.1:27017`, e pará-lo exige elevação.
O contêiner foi publicado em `27018` por um override de Compose não versionado, e
a aplicação apontada para lá por `SPRING_DATA_MONGODB_URI`. A porta do host não
afeta nada do que está na tabela — o que estava sob teste é o entrypoint, o
validador e o que a aplicação grava. Em máquina sem mongod local, o
`docker-compose.yml` do repositório funciona sem override.

### Cobertura — 11/09/2026

| Comando | Testes | Linha | Ramo |
|---|---|---|---|
| `./mvnw clean test` (sem Docker) | 12 | 78,2% — 97/124 | 100% — 6/6 |
| `./mvnw clean verify` (com Docker) | 13 | 99,2% — 123/124 | 100% — 6/6 |

Relatório em `backend/target/site/jacoco/index.html`.
