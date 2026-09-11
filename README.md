# Organizador de estudos — PoC da AEP 2026.2 (6º semestre)

Prova de Conceito de um organizador de tarefas e materiais de estudo, alinhada ao
**ODS 4 — Educação de Qualidade**. Desenvolvida para a Atividade de Estudo
Programada do 6º semestre de Engenharia de Software (Banco de Dados NoSQL,
Paradigmas de Linguagem, Processo de Software, Projeto/Implementação e Testes).

**Esta é a 1ª entrega.** A PoC avaliada neste marco é a **API REST** —
ver [Escopo medido](#escopo-medido-nesta-entrega).

---

## O problema

Quem estuda trabalhando não perde a disciplina por não entender a matéria. Perde
por não ter um lugar único onde o que precisa ser feito e o material para fazer
estejam juntos.

A agenda real do estudante trabalhador vive espalhada: o prazo está num grupo de
mensagens que rolou, o material está num PDF baixado na pasta de downloads, a
data da prova está anotada num papel, e o resto está na memória. Nada disso
conversa. O resultado não é reprovação por nota: é a entrega esquecida, o
trabalho começado na véspera e a disciplina abandonada por acúmulo — uma
desistência por desorganização, não por incapacidade.

**Público.** Estudante que concilia trabalho e estudo: ensino noturno, curso
técnico e graduação, em especial quem não tem rotina de estudo estabelecida nem
ferramenta institucional que resolva as duas metades do problema.

**Relação com o ODS 4.** O ODS 4 trata de educação inclusiva e equitativa, e suas
metas 4.3 e 4.4 falham na prática pela **permanência**, não pelo acesso: quem
desiste normalmente já estava matriculado. Organização de prazos e de material é
uma das causas de abandono que software resolve de forma direta e barata — sem
depender de mudança curricular, de infraestrutura ou de contratação. A PoC ataca
essa causa específica, que é o que torna o vínculo objetivo em vez de retórico.

O problema tem duas metades, e elas correspondem exatamente às duas entregas:

| Metade do problema | Entrega | Modelagem |
|---|---|---|
| tarefas pendentes, com prazo e prioridade | **1ª (esta)** | coleção única `tarefas`, documento achatado |
| materiais de estudo organizados por assunto | 2ª | `categorias` com anotações aninhadas |

## O que já funciona

CRUD completo de tarefas, com validação de entrada, erros padronizados e
validação de esquema no próprio banco.

| Recurso | Situação |
|---|---|
| API REST `/api/tarefas` — criar, listar, buscar, atualizar, excluir | pronto |
| Validação de entrada com mensagens por campo | pronto |
| Erros padronizados (`ApiError` com `fieldErrors`) | pronto |
| Validação de esquema no MongoDB (`$jsonSchema` fechado) | pronto |
| Documentação interativa da API (Swagger UI) | pronto |
| Cliente web mínimo executando o fluxo completo | pronto |
| Telas React de início e de tarefas consumindo a API | listar, criar e concluir |
| Testes automatizados — unitário, contrato HTTP e integração | pronto |
| Login e cadastro | mock, tela apenas — ver [Limites](#limites-conhecidos-desta-entrega) |
| Materiais de estudo (categorias e anotações) | 2ª entrega |

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Backend | Spring Boot 3.5.16 (Web, Validation, Data MongoDB) |
| Banco | MongoDB 7.0 (NoSQL), via Docker Compose |
| Documentação da API | Springdoc OpenAPI (Swagger UI em `/docs`) |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers |
| Cobertura | JaCoCo, com gate de 70% de linha no `verify` |
| Build | Maven (wrapper commitado — não precisa instalar Maven) |
| Cliente mínimo | HTML, CSS e JavaScript nativos, sem build |
| Frontend (em desenvolvimento) | React 19, TypeScript, Vite |

## Estrutura do repositório

```
backend/          API Spring Boot — a PoC medida nesta entrega
  src/main/java/br/com/cesumar/aep/
    model/ repository/ dto/ mapper/ service/ controller/ exception/ configuration/
  src/main/resources/static/crud.html    cliente mínimo, servido pelo Spring
  src/test/java/.../                     unitário, contrato HTTP e integração
banco/init.js     esquema, índice e dados de exemplo da coleção
frontend/         cliente React, em desenvolvimento (fora da medição)
docs/             documentação técnica
docker-compose.yml
```

## Como executar

Pré-requisitos: **JDK 21 ou superior** e **Docker**. Maven não é necessário — o
wrapper está no repositório.

### 1. Subir o banco

```bash
docker compose up -d
```

Sobe o MongoDB 7.0 em `127.0.0.1:27017` e executa `banco/init.js`, que cria a
coleção `tarefas` com o validador de esquema, o índice e três tarefas de exemplo.

> O script roda só na **primeira** subida do contêiner. Depois de alterar
> `banco/init.js`, use `docker compose down -v` (o `-v` apaga o volume) e suba de
> novo — sem isso o script é ignorado em silêncio.
>
> Se a porta 27017 já estiver ocupada por um MongoDB local, o contêiner não sobe.
> Pare o serviço local, ou aponte a aplicação para outra porta com a variável
> `SPRING_DATA_MONGODB_URI`.

### 2. Subir a aplicação

```bash
cd backend
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

### 3. Usar

| Endereço | O que é |
|---|---|
| http://localhost:8080/crud.html | **cliente mínimo** — criar, listar, atualizar e excluir |
| http://localhost:8080/docs | Swagger UI, contrato interativo |
| http://localhost:8080/api/tarefas | a API |

O fluxo principal da PoC se demonstra pelo `crud.html`. Exemplo pela linha de
comando:

```bash
curl -X POST http://localhost:8080/api/tarefas \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Estudar agregações","prazo":"2026-09-20","prioridade":"alta","observacao":"Revisar o capítulo 4"}'
```

Contrato completo, com todos os campos, regras de validação e formato de erro, em
[`docs/api-http.md`](docs/api-http.md).

## Como rodar os testes

São **dois comandos**, e a diferença entre eles importa:

```bash
cd backend

./mvnw clean test      # unitários e de contrato — NÃO exige Docker
./mvnw clean verify    # tudo, incluindo integração e o gate de cobertura
```

| Comando | Testes | Exige Docker | Linha (*node*) | Ramo (*edge*) |
|---|---|---|---|---|
| `./mvnw clean test` | 12 | não | **78,2%** (97/124) | **100%** (6/6) |
| `./mvnw clean verify` | 13 | sim | **99,2%** (123/124) | **100%** (6/6) |

Medido em 11/09/2026. O gate do JaCoCo reprova o build abaixo de **85%** em
linha **e** em ramo — acima dos 70% exigidos pelo enunciado, para travar a
margem já conquistada em vez de permitir que ela caia até o mínimo. Ficam
excluídas a classe `AepApplication` e o pacote `configuration`, que só têm
`main` e `@Bean` — fiação, não comportamento.

Os dois critérios são os de *graph coverage* que o JaCoCo mede: **linha**
corresponde a *node coverage* e **ramo** a *edge coverage*. Critérios mais
fortes — *edge-pair* e *prime path* — nenhuma ferramenta do ecossistema Java
mede automaticamente; onde eles importam, os casos foram derivados à mão do
grafo do método (ver `PrioridadeTest`).

**Por que dois comandos.** O teste de integração usa Testcontainers, que exige
daemon Docker. Numa máquina sem Docker, `verify` falharia e a evidência de
cobertura cairia junto. Por isso os testes unitário e de contrato **sozinhos** já
passam de 70%, por decisão de projeto: o teste de integração é evidência
adicional, nunca a perna que sustenta o número.

### Relatório de cobertura

```bash
cd backend
./mvnw clean test jacoco:report      # sem Docker
# abra backend/target/site/jacoco/index.html
```

Para incluir o teste de integração, troque por `./mvnw clean verify`.

Os três tipos de teste, e o que cada um prova:

| Arquivo | Tipo | Prova |
|---|---|---|
| `TarefaServiceTest` | unitário (Mockito) | regra de negócio |
| `TarefaControllerTest` | contrato (`@WebMvcTest`) | status, forma do JSON, validação |
| `TarefaApiIT` | integração (Testcontainers) | ida e volta contra MongoDB real |

O sufixo decide o executor: `*Test` roda no surefire (`test`), `*IT` no failsafe
(`verify`).

## Escopo medido nesta entrega

O enunciado pede 70% de cobertura "sobre o código da PoC entregue". A PoC deste
marco é a **API em `backend/`**.

O cliente React em `frontend/` está em desenvolvimento e **não faz parte da
medição** — declarado aqui em voz alta, e não por omissão. Ele já consome a API
de verdade nas telas de início e de tarefas (listar, criar e concluir); login,
cadastro e anotações seguem com dados fixos. Seu empacotamento dentro do Spring
e a troca dos mocks restantes por chamadas reais estão planejados para a
2ª entrega.

O fluxo principal da PoC é demonstrado pelo `crud.html`, que é servido pela
própria aplicação e consome a API de verdade.

## Banco de dados

Coleção única `tarefas`, documento achatado, conforme o limite da 1ª entrega
(uma coleção, objetos homogêneos, CRUD básico):

```json
{
  "_id": "ObjectId",
  "titulo": "Estudar agregações do MongoDB",
  "prazo": "2026-09-20T00:00:00.000Z",
  "prioridade": "alta",
  "concluida": false,
  "observacao": "Revisar o capítulo 4",
  "criadaEm": "2026-09-11T21:37:57.282Z",
  "atualizadaEm": "2026-09-11T21:37:57.282Z"
}
```

Todo documento tem todos os campos — ausência e `null` não são estados válidos. O
`$jsonSchema` é **fechado** (`additionalProperties: false`), então campo com nome
errado falha na escrita em vez de divergir em silêncio. Nenhum índice além do
`_id` obrigatório: a listagem não tem filtro, e índice sem consulta que o use é
escrita mais lenta de graça.

Especificação completa, com as alternativas descartadas e o tratamento de fuso
horário, em [`docs/modelagem-banco.md`](docs/modelagem-banco.md).

## Documentação

| Documento | Quando ler |
|---|---|
| [`docs/arquitetura.md`](docs/arquitetura.md) | para entender as camadas e o caminho de uma requisição |
| [`docs/api-http.md`](docs/api-http.md) | para usar a API sem subir a aplicação |
| [`docs/modelagem-banco.md`](docs/modelagem-banco.md) | para entender a modelagem e o porquê dela |
| [`docs/decisoes.md`](docs/decisoes.md) | para saber por que cada escolha foi feita |
| [`docs/padroes.md`](docs/padroes.md) | **antes** de escrever código no backend |
| [`docs/checklist-revisao.md`](docs/checklist-revisao.md) | **depois** de escrever, antes de abrir PR |
| [`docs/backend-tarefas.md`](docs/backend-tarefas.md) | planejamento por tarefa e caminho crítico |
| [`AGENTS.md`](AGENTS.md) | convenções do repositório — commits, branches, Definition of Done |

## Limites conhecidos desta entrega

Declarados porque limite escondido vira pergunta na correção.

- **Login e cadastro são mock.** Não há coleção de usuários, porque a 1ª entrega
  está limitada a uma coleção — criar `usuarios` seria a segunda. As telas
  existem; a autenticação real é a 2ª entrega.
- **A tarefa não tem dono.** Não há campo de usuário nesta entrega, porque a
  coleção `usuarios` é a 2ª entrega e criá-la agora violaria o teto de uma
  coleção só.
- **`GET /api/tarefas` devolve a coleção inteira.** Sem escopo por usuário, sem
  paginação e sem ordenação. O filtro por `usuarioId` entra junto com a
  autenticação real, na 2ª entrega.
- **Última escrita vence.** Duas edições simultâneas da mesma tarefa se
  sobrescrevem.
- **O teste de integração não cobre o validador do banco.** O Testcontainers sobe
  um MongoDB limpo, sem executar `banco/init.js`, logo sem `$jsonSchema` e sem
  índices. Essa conformidade foi verificada à mão contra o Compose, e a evidência
  está registrada em [`docs/decisoes.md`](docs/decisoes.md#verificações-registradas).

## Evolução prevista para a 2ª entrega

Múltiplas coleções, relacionamento entre elas e ao menos uma com objetos
complexos — de forma aditiva, visível em diff:

1. `usuarios` entra como coleção nova, e o login deixa de ser mock;
2. `usuarioId` entra na tarefa, referenciando essa coleção;
3. `categorias` entra com a lista de **anotações aninhada** — o objeto complexo
   exigido, e a outra metade do problema: os materiais de estudo.

## Equipe

| Integrante | Branch |
|---|---|
| Samuel Fuentes Michels | `samuel`, `samuel-backend` |
| Felipe Matrone | `felipe` |

<!-- TODO(samuel): preencher os RAs e o link do vídeo antes de enviar o formulário
     de entrega (seção 11 do enunciado). -->
