# Organizador de estudos — AEP 2026.2 (6º semestre)

Aplicativo para o estudante que trabalha organizar, num lugar só, as tarefas com
prazo e o material de estudo de cada disciplina, alinhado ao **ODS 4 — Educação
de Qualidade**. Desenvolvido para a Atividade de Estudo Programada do 6º semestre
de Engenharia de Software (Banco de Dados NoSQL, Paradigmas de Linguagem,
Processo de Software, Projeto/Implementação e Testes).

**Esta é a 2ª entrega.** O que mudou desde a 1ª, em uma frase: o banco passou de
uma coleção achatada para três coleções relacionadas, uma delas com objetos
aninhados, e cada pessoa passou a entrar com a própria conta e ver só o que é
dela.

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

O problema tem duas metades, e o aplicativo cobre as duas:

| Metade do problema | Onde vive | Entrou na |
|---|---|---|
| tarefas pendentes, com prazo e prioridade | coleção `tarefas` | 1ª entrega |
| materiais de estudo organizados por disciplina | coleção `categorias`, com anotações aninhadas | 2ª entrega |
| de quem é cada coisa | coleção `usuarios`, com login por sessão | 2ª entrega |


## O que funciona

| Recurso | Situação |
|---|---|
| Cadastro, login e logout com senha em BCrypt e sessão | pronto |
| Tarefas — criar, listar, buscar, atualizar, excluir, vincular a uma disciplina | pronto |
| Disciplinas (`categorias`) — CRUD, com cor e descrição | pronto |
| Anotações dentro da disciplina — criar, editar, excluir | pronto |
| Painel de progresso por disciplina (agregação no MongoDB) | pronto |
| Cada pessoa vê só o que é dela | pronto — recurso de outra conta responde `404` |
| Validação de entrada com mensagens por campo e erro padronizado (`ApiError`) | pronto |
| Validação de esquema no próprio MongoDB (`$jsonSchema` fechado nas três coleções) | pronto |
| Documentação interativa da API (Swagger UI) | pronto |
| Testes unitário, de contrato HTTP e de integração contra MongoDB real | pronto |
| Telas React | início e tarefas consomem a API; login, disciplinas e painel em integração |

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Backend | Spring Boot 3.5.16 (Web, Validation, Data MongoDB, Security) |
| Banco | MongoDB 7.0 (NoSQL), via Docker Compose ou instalação local |
| Documentação da API | Springdoc OpenAPI (Swagger UI em `/docs`) |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers |
| Cobertura | JaCoCo, com gate de 70% de linha no `verify` |
| Build | Maven (wrapper commitado — não precisa instalar Maven) |
| Frontend | React 19, TypeScript, Vite |

## Estrutura do repositório

```
backend/          API Spring Boot
  src/main/java/br/com/cesumar/aep/
    model/ repository/ dto/ mapper/ service/ controller/ exception/ configuration/
  src/test/java/.../                     unitário, contrato HTTP e integration/
banco/init.js     esquema, índices e dados de demonstração das três coleções
frontend/         cliente React
docs/             documentação técnica
docker-compose.yml
```

## Como executar

Pré-requisitos: **JDK 21 ou superior** e um **MongoDB** — pelo Docker ou
instalado na máquina. Maven não é necessário: o wrapper está no repositório.

### 1. Subir o banco

Dois caminhos, conforme a máquina. Os dois aplicam o mesmo `banco/init.js`, que
cria as três coleções com validador de esquema, os índices e os dados de
demonstração.

**Com Docker:**

```bash
docker compose down -v && docker compose up -d
```

> O script roda só na **primeira** subida do contêiner. O `down -v` apaga o
> volume e força a releitura — sem ele, uma mudança no `init.js` é ignorada em
> silêncio. Se a porta 27017 já estiver ocupada por um MongoDB local, o
> contêiner não sobe: pare o serviço local ou use o outro caminho.

**Com MongoDB local** na porta 27017, com `mongosh` no PATH:

```bash
mongosh aep --eval 'db.dropDatabase()' && mongosh aep banco/init.js
```

> O `dropDatabase` é obrigatório a partir da segunda vez: o script cria as
> coleções com `createCollection`, que falha se elas já existem.

### 2. Subir a aplicação

```bash
cd backend
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

### 3. Entrar

O seed cria uma conta de demonstração, já com disciplinas, anotações e tarefas —
uma delas atrasada, para o painel ter o que mostrar:

| E-mail | Senha |
|---|---|
| `demo@aep.local` | `aep-demo-2026` |

A senha está aqui de propósito, para quem corrige conseguir entrar. Está listada
em [Limites conhecidos](#limites-conhecidos).

| Endereço | O que é |
|---|---|
| http://localhost:8080/docs | Swagger UI, contrato interativo de todas as rotas |
| http://localhost:8080/api/… | a API (rotas abaixo) |

O `crud.html` da 1ª entrega continua no repositório, mas agora responde `401`:
ele ficou atrás da autenticação e não sabe fazer login. Não quebrou — saiu da
demonstração.

## A API

Todas as rotas em `application/json`. Cadastro e login são públicos; **todo o
resto de `/api/**` exige sessão**.

| Método | Caminho | O que faz |
|---|---|---|
| `POST` | `/api/usuarios` | cadastra — `201`, ou `409` se o e-mail já existe |
| `POST` | `/api/sessao` | login — `200`, ou `401` com senha errada |
| `GET` | `/api/sessao` | quem está logado — `200`, ou `401` |
| `DELETE` | `/api/sessao` | logout — `204` |
| `GET` `POST` | `/api/tarefas` | listar e criar tarefas |
| `GET` `PUT` `DELETE` | `/api/tarefas/{id}` | uma tarefa |
| `GET` `POST` | `/api/categorias` | listar e criar disciplinas |
| `GET` `PUT` `DELETE` | `/api/categorias/{id}` | uma disciplina, com as anotações |
| `POST` | `/api/categorias/{id}/anotacoes` | criar anotação na disciplina |
| `PUT` `DELETE` | `/api/categorias/{id}/anotacoes/{anotacaoId}` | uma anotação |
| `GET` | `/api/painel` | pendentes, concluídas, atrasadas e progresso por disciplina |

A anotação é endereçada pela disciplina que a contém porque ela não existe fora
dela: a URL conta a verdade da modelagem.

### Sessão e CSRF

O login cria uma sessão (cookie `JSESSIONID`). A proteção contra CSRF fica
**ligada**: o servidor planta o cookie `XSRF-TOKEN` já na primeira resposta, e o
cliente devolve esse valor no cabeçalho `X-XSRF-TOKEN` em toda requisição que
não é `GET`. O login troca o id da sessão e descarta o token, contra fixação de
sessão: a resposta do login **apaga** o `XSRF-TOKEN`, e o novo só chega na
resposta seguinte. Por isso o cliente faz um `GET /api/sessao` depois do login
antes da primeira escrita — sem ele, a escrita volta `403`.

Pela linha de comando, o fluxo completo:

```bash
# 1. qualquer requisição planta o XSRF-TOKEN (esta volta 401, e tudo bem)
curl -s -c c.txt -b c.txt http://localhost:8080/api/sessao
xsrf() { awk '/XSRF-TOKEN/ {print $7}' c.txt; }

# 2. login
curl -s -c c.txt -b c.txt -H "X-XSRF-TOKEN: $(xsrf)" \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@aep.local","senha":"aep-demo-2026"}' \
  http://localhost:8080/api/sessao

# 3. o login apagou o token: um GET traz o novo
curl -s -c c.txt -b c.txt http://localhost:8080/api/sessao

# 4. escrita, com o token novo
curl -s -c c.txt -b c.txt -H "X-XSRF-TOKEN: $(xsrf)" \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Estudar MongoDB","prazo":"2026-11-20","prioridade":"alta"}' \
  http://localhost:8080/api/tarefas

curl -s -b c.txt http://localhost:8080/api/painel
```

Três comportamentos que parecem erro e não são:

- **Recurso de outra conta responde `404`, não `403`.** `403` confirmaria que o
  recurso existe. O pertencimento é conferido no Service, não na rota.
- **O `401` vem em dois formatos.** Sem sessão, o corpo vem vazio (é a cadeia do
  Spring Security que responde); com senha errada, vem como `ApiError`. Nos dois
  casos quem decide é o status.
- **Depois do logout o cookie `XSRF-TOKEN` continua no navegador** até a próxima
  resposta reescrevê-lo. O token mora no cookie, não na sessão, e sem sessão
  qualquer requisição volta `401`.

O contrato da 1ª entrega, com os campos de tarefa e o formato do erro, está em
[`docs/api-http.md`](docs/api-http.md); o das rotas novas está no Swagger.

## Banco de dados

Três coleções, dois relacionamentos e um objeto complexo:

```
usuarios  ◀── usuarioId ──  categorias  { anotacoes: [ … ] }
    ▲                            ▲
    └──── usuarioId ── tarefas ──┘ categoriaId (opcional)
```

| Coleção | O que guarda | Referências |
|---|---|---|
| `usuarios` | `nome`, `email` (único, minúsculo), `senhaHash` (BCrypt) | — |
| `categorias` | a disciplina, com `cor` e a lista **`anotacoes` aninhada** | `usuarioId` |
| `tarefas` | `titulo`, `prazo`, `prioridade`, `concluida`, `observacao` | `usuarioId` (obrigatório), `categoriaId` (opcional) |

**Por que a anotação é aninhada.** Ela não existe fora da disciplina, é sempre
lida junto com ela e nunca é consultada sozinha — o caso de manual para
subdocumento. A tarefa, ao contrário, tem vida própria (a tela de tarefas lista
todas, de todas as disciplinas), por isso fica em coleção própria e referencia a
disciplina.

**Por que `categoriaId` é opcional.** Tarefa sem disciplina é válida, e o painel
a mostra num balde "Sem disciplina" — que só aparece quando existe alguma.
Excluir uma disciplina desvincula as tarefas dela em vez de apagá-las.

**O painel é uma agregação.** `GET /api/painel` parte de `categorias` filtradas
pelo dono e faz `$lookup` em `tarefas`. As referências são gravadas como
`string` e o `_id` é `ObjectId`, então o `$lookup` usa `let` + `$expr` +
`$toString` — a forma simples (`localField`/`foreignField`) compara tipos
diferentes e devolve vazio **sem erro nenhum**. Há teste de integração contra
MongoDB real provando isso.

Todo o esquema é validado no próprio banco por `$jsonSchema` fechado
(`additionalProperties: false`): campo com nome errado falha na escrita em vez
de divergir em silêncio. Os índices são três — `usuarios.email` único,
`categorias.usuarioId` e `tarefas.{usuarioId, prazo}` —, um por consulta que os
usa.

Especificação e alternativas descartadas em
[`docs/segunda-entrega.md`](docs/segunda-entrega.md#modelagem) e
[`docs/modelagem-banco.md`](docs/modelagem-banco.md).

## Como rodar os testes

```bash
cd backend

./mvnw clean test      # unitários e de contrato — NÃO exige Docker
./mvnw clean verify    # tudo, incluindo integração e o gate de cobertura
```

| Comando | Testes | Exige Docker | Linha (*node*) | Ramo (*edge*) |
|---|---|---|---|---|
| `./mvnw clean test` | 86 | não | **97,4%** (456/468) | **92,9%** (26/28) |
| `./mvnw clean verify` | 86 + 5 de integração | sim | **98,9%** (463/468) | **92,9%** (26/28) |

Medido em 09/11/2026, com Docker ligado e os dois comandos verdes. O gate do
JaCoCo reprova o build abaixo de **70% de linha**. Ficam fora do denominador
`AepApplication` e o pacote `configuration`, que só têm `main` e `@Bean` —
fiação, não comportamento.

Sem Docker, `./mvnw clean verify -DskipITs` roda o gate sem os testes de
integração. Os testes sem Docker **sozinhos** já passam de 70%, por decisão de
projeto: a integração é evidência adicional, nunca a perna que sustenta o
número.

Os tipos de teste, e o que cada um prova:

| Tipo | Exemplos | Prova |
|---|---|---|
| unitário (Mockito) | `TarefaServiceTest`, `CategoriaServiceAnotacaoTest`, `PainelServiceTest` | regra de negócio, pertencimento, fronteira do "atrasada" |
| contrato (`@WebMvcTest`) | `TarefaControllerTest`, `SessaoControllerTest`, `TarefaControllerSegurancaTest` | status, forma do JSON, validação, `401` e CSRF |
| integração (Testcontainers) | `PainelApiIT`, `ValidadorMongoIT`, `TarefaApiIT` | a agregação e o `$jsonSchema` do `init.js` contra MongoDB real |

O sufixo decide o executor: `*Test` roda no surefire (`test`), `*IT` no failsafe
(`verify`). Relatório de cobertura em `backend/target/site/jacoco/index.html`
depois de qualquer um dos dois comandos acrescido de `jacoco:report`.

## Documentação

| Documento | Quando ler |
|---|---|
| [`docs/segunda-entrega.md`](docs/segunda-entrega.md) | desenho desta entrega — modelagem, segurança, painel |
| [`docs/plano-segunda-entrega.md`](docs/plano-segunda-entrega.md) | o plano tarefa a tarefa, com os grupos de revisão |
| [`docs/arquitetura.md`](docs/arquitetura.md) | as camadas e o caminho de uma requisição |
| [`docs/modelagem-banco.md`](docs/modelagem-banco.md) | modelagem, índices, datas e fuso |
| [`docs/api-http.md`](docs/api-http.md) | contrato de tarefas sem subir a aplicação |
| [`docs/decisoes.md`](docs/decisoes.md) | por que cada escolha foi feita |
| [`docs/padroes.md`](docs/padroes.md) | **antes** de escrever código no backend |
| [`docs/checklist-revisao.md`](docs/checklist-revisao.md) | **depois** de escrever, antes de abrir PR |
| [`AGENTS.md`](AGENTS.md) | convenções do repositório — commits, branches, Definition of Done |

## Versionamento

Uma branch por pessoa (`samuel-backend`, `felipe`). Cada grupo de tarefas do
plano termina em PR para **`dev`**, a branch de integração, onde ninguém commita
direto. `main` recebe `dev` só no fim, e a tag da entrega sai na `main`.

## Limites conhecidos

Declarados porque limite escondido vira pergunta na correção.

- **Anotações aninhadas não escalam sem teto.** O documento da disciplina cresce
  com cada anotação, e 16 MB é o limite rígido do BSON — por isso o `conteudo`
  tem teto de 20 000 caracteres. Se um dia importar, `anotacoes` vira coleção
  própria com `categoriaId`.
- **Última escrita vence.** Duas edições simultâneas da mesma tarefa ou anotação
  se sobrescrevem. Bloqueio otimista por versão resolve sem mexer na modelagem.
- **Excluir disciplina não é atômico.** São duas escritas em coleções diferentes
  num MongoDB standalone, sem transação. A ordem — desvincular as tarefas antes
  de apagar a disciplina — garante que uma falha no meio deixe tarefa sem
  disciplina, e nunca tarefa apontando para disciplina inexistente.
- **Sessão em memória.** Reiniciar o servidor desloga todo mundo.
- **A senha de demonstração está no repositório.** Dado fictício, para quem
  corrige conseguir entrar. Num sistema real, seed com credencial versionada não
  passa.
- **Sem recuperação de senha.** Fora do escopo da PoC.
- **O `conteudo` da anotação é HTML guardado como veio**, limitado em tamanho. A
  sanitização acontece na renderização, no frontend.

## Equipe

| Integrante | Branch |
|---|---|
| Samuel Fuentes Michels | `samuel`, `samuel-backend` |
| Felipe Matrone | `felipe` |

<!-- TODO(samuel): preencher os RAs e o link do vídeo antes de enviar o formulário
     de entrega (seção 11 do enunciado). -->
