# Segunda entrega — desenho da solução

> Documento de desenho do 2º bimestre. Entrega em **13/11/2026**.
> **Implementado.** O que a implementação decidiu onde este desenho deixou em
> aberto está em "O que fica para quem implementa", no fim; o estado de uso
> (como subir, credenciais, cobertura medida) está no `README.md`.
> Modelagem da 1ª entrega: `docs/modelagem-banco.md`. Convenções de código:
> `docs/padroes.md`. Camadas: `docs/arquitetura.md`.

## Por que este documento existe

A 1ª entrega tirou **0,6 de 1,0**, com o comentário de que "o banco e o projeto
estão muito simples". Não houve detalhamento por critério, e a equipe decidiu não
pedir revisão.

Sem o detalhamento, vale olhar a aritmética da rubrica. Dos oito critérios da 1ª
entrega, sete valem 0,1 e **o vídeo vale 0,3**. "Banco de dados NoSQL" é um único
item de 0,1 — perder 0,4 não cabe nele. O vídeo mostrou Swagger, contexto do
projeto e o relatório de cobertura: uma demonstração para desenvolvedor, não a
PoC resolvendo o problema de alguém.

A conclusão que orienta este desenho: **a nota não caiu só no banco, e ampliar a
modelagem sozinho não a recupera.** O entregável central da 2ª entrega é o
aplicativo funcionando. As coleções novas são o que sustenta isso.

Vale registrar que a modelagem da 1ª entrega estava correta: a seção 8 do
enunciado manda, para aquele marco, uma coleção única com objetos homogêneos e
CRUD básico. Foi o que foi entregue.

## O que a rubrica cobra nesta entrega

| Critério | Pontos | Onde é atendido |
|---|---|---|
| PoC funcional e evolução | 0,1 | app com Anotações e Categorias funcionando |
| Banco de dados NoSQL | 0,1 | 3 coleções, 2 relacionamentos, subdocumentos |
| POO e qualidade do código | 0,1 | camadas de `docs/padroes.md` mantidas |
| Metodologia de trabalho | 0,1 | quadro Trello `AEP6S-2°Bim` |
| Testes automatizados | 0,1 | TDD, testes por camada |
| Cobertura ≥ 70% | 0,1 | gate JaCoCo no `verify` |
| Documentação técnica | 0,1 | README, este documento, `modelagem-banco.md` |
| GitHub e versionamento | 0,1 | fluxo `dev` → `main` + tag da versão final |
| Vídeo de demonstração | 0,2 | roteiro na seção "Vídeo" |

Metade da nota — 0,5 — não é código: metodologia, documentação, versionamento e
vídeo. Foi onde a 1ª entrega sangrou.

## Objetivos

1. Atender integralmente a seção 8 do enunciado: múltiplas coleções,
   relacionamento entre elas e pelo menos uma coleção com objetos complexos.
2. Entregar o aplicativo **usável de ponta a ponta**: cadastrar, entrar,
   organizar tarefas por disciplina, escrever anotações, ver progresso.
3. Manter o gate de 70% de cobertura verde durante todo o percurso, não só no fim.
4. Deixar a evolução **visível** — em diff, em quadro, em documento e em vídeo.

## Não-objetivos

- Aplicativo multiusuário em produção. É uma PoC; os limites ficam documentados.
- Recursos que não aparecem na demonstração.
- Refatorar o que já funciona da 1ª entrega sem motivo ligado à evolução.
- Quarta coleção para engrossar o número. Profundidade, não largura.

## Anti-objetivos

Coisas que, se acontecerem, tornam esta entrega um fracasso mesmo que o código
compile:

- **Cobertura abaixo de 70%.** Zera o item — e código novo de duas coleções
  entrando sem teste junto derruba o percentual antes de levantá-lo.
- **Vídeo mostrando Swagger de novo.** Repete exatamente o erro diagnosticado.
- **Felipe bloqueado esperando a API.** O caminho crítico passa pelo backend; se
  o contrato não existir cedo, as telas não integram a tempo.
- **Quadro preenchido na véspera.** Evidência de método é movimentação ao longo
  do período, não um despejo de cards no dia 12.

## Restrições

| Restrição | Consequência |
|---|---|
| 5 dias corridos (08/11 → 13/11), 2 pessoas | não cabe largura; cabe profundidade |
| Caminho crítico no backend | contrato antes da implementação |
| `./mvnw clean test` não pode exigir Docker | testes de unidade e MockMvc sustentam os 70%; `*IT` é evidência adicional |
| JaCoCo exclui `configuration/**` | `SecurityConfiguration` não entra no denominador |
| Telas de Anotações já existem com dados locais | o contrato da API se adapta a elas, não o contrário |
| Mongo rodando nativo na 27017 nesta máquina | `banco/init.js` precisa de caminho `mongosh`, não só Compose |

## Decisões já travadas

Decididas por Samuel, não são para rediscutir na implementação:

- **Spring Security**, e não um interceptor caseiro. O conteúdo foi dado em aula
  no bimestre passado. Custa mais, mas é o que a equipe sabe defender.
- **TDD** na implementação do backend.
- **Branch `dev`** integrando o trabalho dos dois, e só depois `main` — com a tag
  da versão final na `main`.
- Divisão: backend, banco e segurança com Samuel; telas e integração com Felipe.

Decisões tomadas neste documento estão marcadas como **[decisão do desenho]** e
podem ser contestadas por quem implementa.

## Modelagem

Três coleções. Duas referências entre coleções. Uma lista de subdocumentos.

```
usuarios ──< categorias ──┐ (anotacoes[] aninhadas)
    │                     │
    └──────< tarefas >────┘ (categoriaId opcional)
```

### `usuarios`

| Campo | Tipo | Regra |
|---|---|---|
| `_id` | ObjectId | |
| `nome` | string | obrigatório |
| `email` | string | obrigatório, **único**, minúsculo |
| `senhaHash` | string | obrigatório, BCrypt |
| `criadoEm` | date | obrigatório |

Nunca sai pela fronteira HTTP com `senhaHash`. O `UsuarioResponse` tem `id`,
`nome` e `email`, e nada mais.

### `categorias` — a coleção com objetos complexos

| Campo | Tipo | Regra |
|---|---|---|
| `_id` | ObjectId | |
| `usuarioId` | string | obrigatório, referencia `usuarios`. É `string` porque o model guarda `String` e o Spring Data só coage para ObjectId o campo `@Id` |
| `nome` | string | obrigatório |
| `descricao` | string | opcional |
| `cor` | string | enum `roxo`, `magenta`, `verde`, `ambar` |
| `criadaEm` / `atualizadaEm` | date | obrigatórios |
| `anotacoes` | array de subdocumentos | obrigatório, pode ser vazio |

Subdocumento de anotação:

| Campo | Tipo | Regra |
|---|---|---|
| `id` | string | ObjectId gerado no Service **[decisão do desenho]** |
| `titulo` | string | obrigatório |
| `conteudo` | string | HTML, máximo 20 000 caracteres |
| `criadaEm` / `atualizadaEm` | date | obrigatórios |

Três decisões aqui:

- **O `cor` repete o enum que o frontend já usa.** Inventar outro vocabulário
  obrigaria um mapa de tradução em algum lugar, e esse lugar sempre vira bug.
- **A anotação não tem `_id` próprio porque não é documento, é subdocumento.** O
  Mongo não gera id para elemento de array; o Service gera um `ObjectId` e grava
  como string no campo `id`. Sem isso, não há como endereçar uma anotação para
  editar ou excluir. **[decisão do desenho]**
- **O limite de 20 000 caracteres existe por causa do teto de 16 MB do
  documento.** Com anotações aninhadas, uma categoria muito usada cresce sem
  limite natural. É validação de entrada em fronteira, não otimização.

### `tarefas` — o que muda

Dois campos novos no documento existente:

| Campo | Tipo | Regra |
|---|---|---|
| `usuarioId` | string | **obrigatório**, referencia `usuarios`. `string` porque o model guarda `String`; só o `_id` é coagido para ObjectId |
| `categoriaId` | string | **opcional**, referencia `categorias`. `string` pelo mesmo motivo: o model guarda `String` |

`categoriaId` é opcional de propósito **[decisão do desenho]**: nem toda tarefa
pertence a uma disciplina, as tarefas que já existem não têm nenhuma, e
obrigá-la quebraria a tela de Tarefas e o seed de demonstração. O relacionamento
que a seção 8 pede continua existindo e demonstrável.

O que **não** se guarda, seguindo a regra já estabelecida em
`docs/modelagem-banco.md` — campo derivado guardado é campo que vai mentir:

- a contagem de anotações da categoria (`anotacoes: 12` no mock) é `anotacoes.length`;
- o progresso por disciplina é agregação, não contador.

### Índices

Agora existem consultas que os justificam, o que não era verdade na 1ª entrega:

| Coleção | Índice | Por quê |
|---|---|---|
| `usuarios` | `{ email: 1 }`, único | o login busca por e-mail; a unicidade é regra, não convenção |
| `categorias` | `{ usuarioId: 1 }` | toda listagem de categoria é por dono |
| `tarefas` | `{ usuarioId: 1, prazo: 1 }` | listagem e calendário filtram por dono e ordenam por prazo |

### `banco/init.js`

O arquivo precisa ser reescrito, e **não é opcional**: o `$jsonSchema` das
tarefas está com `additionalProperties: false` e `required` fechado. No instante
em que `usuarioId` e `categoriaId` entrarem, o validator rejeita todo insert e o
seed para de subir.

O novo `init.js` cria as três coleções com validador, cria os índices acima e
semeia um usuário de demonstração com categorias, anotações e tarefas já
vinculadas — o suficiente para a tela não abrir vazia na gravação do vídeo.

**`init.js` é reescrito uma vez, não duas.** As três coleções e o `usuarioId`
obrigatório em `tarefas` entram já na etapa 3, antes da Security existir. Entre a
etapa 3 e a etapa 4 o backend usa o id do usuário semeado como constante, e a
etapa 4 só troca a origem desse id: de constante para sessão autenticada.
Escrever o validador agora sem `usuarioId` e voltar nele depois é pagar o mesmo
trabalho duas vezes e correr o risco de errar na segunda. **[decisão do desenho]**

O banco de desenvolvimento é descartável e **é derrubado e semeado de novo**; não
há migração de dados existentes, porque não há dado que não venha do próprio
`init.js`.

A senha do usuário semeado vai como **hash BCrypt pré-computado** no script —
`mongosh` não tem BCrypt, e fazer o seed subir pela aplicação acoplaria o seed ao
boot do Spring. O e-mail e a senha de demonstração ficam escritos no README: é
credencial de dado fictício em repositório acadêmico, e o vídeo precisa que o
login funcione na primeira tentativa. **[decisão do desenho]**

Como o Mongo nesta máquina roda nativo e não via Compose, o README precisa
documentar os dois caminhos: `docker compose down -v && docker compose up -d`
para quem usa container, e `mongosh <banco> banco/init.js` para quem usa local.

## Contrato da API

Este é o ponto de encontro entre as duas pessoas. **Ele existe antes da
implementação**: no primeiro dia os endpoints sobem devolvendo dados fixos, com o
OpenAPI correto, para o Felipe integrar enquanto o recheio vira Mongo de verdade.

| Método | Rota | Devolve |
|---|---|---|
| `POST` | `/api/usuarios` | 201, usuário criado (cadastro, público) |
| `POST` | `/api/sessao` | 200, usuário autenticado (login, público) |
| `DELETE` | `/api/sessao` | 204 (logout) |
| `GET` | `/api/sessao` | 200, usuário da sessão atual |
| `GET` | `/api/categorias` | lista de categorias do usuário, com `totalAnotacoes` |
| `POST` | `/api/categorias` | 201, categoria criada |
| `GET` | `/api/categorias/{id}` | categoria **com `anotacoes` aninhadas** |
| `PUT` | `/api/categorias/{id}` | categoria atualizada |
| `DELETE` | `/api/categorias/{id}` | 204 |
| `POST` | `/api/categorias/{id}/anotacoes` | 201, anotação criada |
| `PUT` | `/api/categorias/{id}/anotacoes/{anotacaoId}` | anotação atualizada |
| `DELETE` | `/api/categorias/{id}/anotacoes/{anotacaoId}` | 204 |
| `GET` | `/api/painel` | agregação de progresso (ver abaixo) |

As rotas de tarefas continuam as mesmas; `categoriaId` entra nos DTOs de criação
e atualização, e a listagem passa a devolver só as tarefas do usuário da sessão.

### Formatos na fronteira

`frontend/src/pages/Anotacoes/dados.ts` é a **autoridade sobre os nomes dos
campos e os slugs de `cor`**, com três correções obrigatórias — todas já
previstas em `docs/modelagem-banco.md`:

| No mock | Na API | Por quê |
|---|---|---|
| `id: 1` (number) | `id: "672f…"` (string) | ObjectId não é inteiro sequencial |
| `anotacoes: 12` | `totalAnotacoes: 12` | o nome `anotacoes` fica com a lista real |
| `atualizadaEm: "Editado hoje"` | `atualizadaEm: "2026-11-08T14:32:00Z"` | texto de exibição se formata na tela, não no banco |

```jsonc
// GET /api/categorias
[{ "id": "…", "nome": "Engenharia de Software", "descricao": "…",
   "cor": "roxo", "totalAnotacoes": 12 }]

// GET /api/categorias/{id}
{ "id": "…", "nome": "…", "descricao": "…", "cor": "roxo",
  "anotacoes": [{ "id": "…", "titulo": "…", "conteudo": "<div>…</div>",
                  "criadaEm": "…", "atualizadaEm": "…" }] }

// GET /api/sessao  |  POST /api/sessao  |  POST /api/usuarios
{ "id": "…", "nome": "…", "email": "…" }

// GET /api/painel
{ "pendentes": 7, "concluidas": 12, "atrasadas": 2,
  "progresso": [{ "categoriaId": "…", "nome": "…", "cor": "roxo",
                  "total": 5, "concluidas": 3, "percentual": 60 }] }
```

`percentual` vem inteiro e já arredondado do backend. Dois lugares arredondando o
mesmo número é um lugar a mais para divergir.

### Exclusão de categoria

`DELETE /api/categorias/{id}` **desvincula** as tarefas antes de apagar: um
`updateMany` zerando `categoriaId`, depois o delete. Nesta ordem de propósito —
se falhar no meio, o pior caso é tarefa sem disciplina, não tarefa apontando para
categoria que não existe. Referência pendurada quebraria o `$lookup` do painel em
silêncio. **[decisão do desenho]**

Não é atômico num Mongo standalone, e isso está em "Limites conhecidos".

Duas formas deliberadas:

- **A listagem de categorias não traz as anotações.** A tela inicial só mostra
  nome, cor e contagem; trazer o conteúdo de todas as anotações para desenhar
  cards seria carregar megabytes para exibir um número.
- **A anotação é endereçada pela categoria que a contém**
  (`/categorias/{id}/anotacoes/{anotacaoId}`), porque ela não existe fora dela. A
  URL conta a verdade da modelagem.

## Segurança

Spring Security com sessão, conforme decidido.

- Senha em **BCrypt**, via `PasswordEncoder`. Senha em texto puro não existe no
  banco, no log nem na resposta.
- `SecurityFilterChain` em `configuration/SecurityConfiguration.java` — a pasta
  já é excluída do JaCoCo, então a configuração não dilui a cobertura.
- Públicos: `POST /api/usuarios`, `POST /api/sessao`, `/docs/**`,
  `/v3/api-docs/**` e os estáticos. Todo o resto de `/api/**` exige autenticação.
- **Pertencimento é regra de negócio, não de rota.** O Security responde "quem é
  você"; quem responde "isso é seu" é o Service, comparando `usuarioId` antes de
  ler, alterar ou excluir. Recurso de outro usuário devolve **404, não 403** —
  403 confirma que o recurso existe. **[decisão do desenho]**
- **CSRF fica ligado**, com `CookieCsrfTokenRepository.withHttpOnlyFalse()`, e o
  frontend envia o header `X-XSRF-TOKEN`. É a resposta padrão do Spring para SPA
  com sessão. **[decisão do desenho — tem custo no lado do Felipe]**
- **Login e cadastro não ficam isentos de CSRF**, e não precisam de isenção: o
  app chama `GET /api/sessao` ao abrir para saber se já existe alguém logado, e é
  essa chamada que planta o cookie do token. A requisição que o app já fazia por
  outro motivo resolve o problema. Isentar duas rotas de POST seria abrir buraco
  para economizar uma chamada que existe de qualquer forma. **[decisão do desenho]**
- **Não existe chave para desligar a Security.** Flag de "roda sem autenticação"
  é a que vaza para produção. Se o Felipe precisar trabalhar antes da Security
  subir, o que destrava ele é o contrato com dados fixos da etapa 1, não um
  interruptor.

Dois detalhes de status que o Service precisa respeitar:

- **E-mail repetido devolve 409**, não 400. O índice único é o guarda final, e o
  `DuplicateKeyException` é mapeado para 409 como rede de segurança — mesmo com a
  checagem no Service, duas requisições simultâneas passam pelas duas checagens.
- **`categoriaId` de outro usuário, ou inexistente, na criação ou edição de
  tarefa, devolve 404**, pela mesma regra de cima: 400 confirmaria que o id
  existe e só não é seu.

O `conteudo` da anotação é HTML e vai para a tela. O backend guarda o que
recebeu, limitado em tamanho; **a sanitização acontece na renderização**, no
frontend, com DOMPurify — está no critério de aceitação do card 12. É XSS
armazenado se esquecido, então não é item de acabamento. **[decisão do desenho]**

## Painel — a agregação

Um endpoint, `GET /api/painel`, construído com *aggregation pipeline* de verdade:
`$match` pelo usuário e `$lookup` ligando `categorias` a `tarefas` (só as do
mesmo dono). As contagens ficaram em Java, no `PainelService` — ver "O que fica
para quem implementa".

Ele devolve: total de pendentes, concluídas e atrasadas, e o progresso por
disciplina (concluídas sobre total, por categoria).

Os termos precisam de definição fechada, senão cada camada inventa a sua:

- **Atrasada** é tarefa não concluída com `prazo` anterior a hoje. "Hoje" vem do
  `Clock` injetado, em UTC, como já é a regra do repositório — o que também faz a
  contagem ser testável sem esperar o dia virar.
- **Tarefa sem `categoriaId`** aparece no progresso num balde próprio, com
  `categoriaId: null` e `nome: "Sem disciplina"`. Omitir faria a soma dos
  parciais não fechar com os totais, e aí o painel mente.
- **Categoria sem tarefa nenhuma** aparece com `total: 0` e `percentual: 0`.
  Disciplina recém-criada tem que aparecer na tela; sumir seria bug.
- Os widgets de "hoje" da tela Inicio **continuam no cliente**. Eles filtram uma
  lista que a tela já tem em mãos; trazer um endpoint para isso é uma ida ao
  servidor para não fazer um `filter`. **[decisão do desenho — o card 13 é do
  Felipe; se ele preferir servidor, é conversa dele]**

Duas razões, e as duas importam:

1. É o que faz "uso efetivo de NoSQL" significar mais do que "guardamos JSON". O
   professor que chamou o banco de simples vai ver consulta, não só CRUD.
2. É o backend que o card 14 do Felipe exige quando diz "métricas do Dashboard
   não usam números fixos". Esse endpoint não estava em card nenhum — é um
   buraco no quadro, não um extra.

**Aviso para quem implementa o painel: `usuarioId` e `categoriaId` são `string`,
`_id` é ObjectId.** O model guarda `String` e o Spring Data só coage o campo
`@Id`. Por isso um `$lookup` de `categorias` para `tarefas` com
`localField`/`foreignField` **não casa**: `$lookup` compara tipos BSON exatos, e
`ObjectId("…b001")` nunca é igual a `"…b001"`. O resultado não é erro, é array
vazio — o painel mostraria toda categoria com total 0 e toda tarefa em "Sem
disciplina", com build verde e banco correto. O pipeline tem de usar a forma com
`let` + `pipeline` + `$expr` (Mongo 7.0 suporta):

```js
{ $lookup: {
    from: "tarefas",
    let: { catId: "$_id" },
    pipeline: [ { $match: { $expr: { $eq: ["$categoriaId", { $toString: "$$catId" }] } } } ],
    as: "tarefas"
} }
```

## Ordem de implementação

A ordem não é arbitrária; ela protege o que é avaliado:

| # | Etapa | Por quê nesta posição |
|---|---|---|
| 1 | Contrato da API no ar com dados fixos + OpenAPI | destrava o Felipe no dia 1 |
| 2 | `categorias` + `anotacoes[]` + `categoriaId` em tarefas | **é o requisito graduado**; entra em disco antes de qualquer risco |
| 3 | `banco/init.js` reescrito + índices | sem isso nada sobe |
| 4 | `usuarios` + Spring Security + pertencimento nos Services | peça mais cara e mais arriscada, e a rubrica não a pede |
| 5 | `GET /api/painel` | depende de usuário e categoria existirem |
| 6 | README, este documento revisado, tag | documentação é 0,1 e versionamento é 0,1 |
| 7 | Vídeo | 0,2, e precisa do app inteiro de pé |

Spring Security é o item 4 e não o item 1 de propósito. Se o tempo acabar no
meio dele, o requisito da seção 8 já está cumprido e demonstrável. Na ordem
inversa, uma peça que a rubrica não cobra levaria junto o item que ela cobra.

## Testes e cobertura

TDD: o teste nasce antes do código, não depois. O card 11 do Trello ("Ampliar
testes automatizados") descreve o resultado, não uma etapa final — tratá-lo como
etapa final é a forma mais cara de perder 0,1.

Matriz, seguindo `docs/padroes.md` — cada comportamento testado em **uma** camada:

| Camada | O que é coberto |
|---|---|
| Service | regra: pertencimento, unicidade de e-mail, anotação dentro da categoria certa, derivação de datas |
| Controller (MockMvc) | contrato: status, validação por campo, 401 sem sessão, 404 de recurso alheio |
| Integração (`*IT`, Testcontainers) | o pipeline do painel e o validador do `$jsonSchema` contra Mongo real |

`./mvnw clean test` continua passando **sem Docker**: os testes de Service e
MockMvc sozinhos sustentam os 70%.

**O painel precisa de um cuidado específico, ou o endpoint que reforça o critério
de NoSQL derruba o de cobertura.** Se o pipeline morar numa classe que só o `*IT`
exercita, aquelas linhas entram no denominador do JaCoCo e não no numerador — e
`./mvnw clean test` sem Docker não as cobre. Então **montar** o pipeline e
**executá-lo** ficam separados: um método devolve o `Aggregation`, e um teste
unitário afirma sobre os estágios dele sem Mongo nenhum; outro método, de duas
linhas, executa. O `*IT` continua provando que o pipeline roda de verdade, como
evidência adicional. **[decisão do desenho]**

## Vídeo

3 a 5 minutos. O erro da 1ª entrega não se repete: **a demonstração é o
aplicativo**.

1. O problema e o ODS 4 — 30 s, falando do estudante que trabalha.
2. O app: cadastrar, entrar, criar a disciplina, escrever a anotação, criar a
   tarefa ligada à disciplina, ver o painel mudar — 2 min.
3. A evolução desde a 1ª entrega: de uma coleção achatada para três coleções com
   subdocumentos, mostrada no Compass — 45 s.
4. Arquitetura e tecnologias — 30 s.
5. Testes e cobertura, com o relatório na tela — 30 s.

Swagger e JaCoCo entram no fim, como evidência. Não como o corpo.

## O que fica para quem implementa

Era a lista do que o desenho não fechava. Como ficou:

- **Nomes**: seguem `docs/padroes.md`; exceções de recurso inexistente herdam de
  `RecursoNaoEncontradoException`, o que dá um handler só para os três `404`.
- **Pipeline do painel**: o banco faz só `$match` + `$lookup` (com `let`,
  `$expr` e `$toString`, porque as referências são `string` e `_id` é
  `ObjectId`). As contas — atrasada, percentual, o balde "Sem disciplina" —
  ficaram no `PainelService`, em Java, onde a fronteira de "hoje" é testável com
  `Clock` fixo. Ver `docs/arquitetura.md`.
- **Usuário semeado**: `demo@aep.local` / `aep-demo-2026`.
- **Serviço de anotações**: não ganhou classe própria; vive em
  `CategoriaService`, que ainda se lê inteiro.

## Limites conhecidos

A honestidade sobre limites já é prática do repositório e será mantida no README:

- **Anotações aninhadas não escalam sem teto.** O documento da categoria cresce
  com cada anotação, e 16 MB é o limite rígido do BSON. O caminho, se um dia
  importar, é promover `anotacoes` a coleção própria com `categoriaId`.
- **Última escrita vence.** Duas edições simultâneas da mesma anotação se
  sobrescrevem. Bloqueio otimista por versão resolve sem mexer na modelagem.
- **Excluir categoria não é atômico.** São duas escritas em coleções diferentes
  num Mongo standalone, que não tem transação. A ordem escolhida — desvincular
  antes de apagar — garante que a falha no meio deixe tarefa sem disciplina, e
  nunca tarefa apontando para categoria inexistente. Transação multi-documento
  exigiria replica set.
- **Senha do usuário de demonstração está no repositório.** É dado fictício de
  trabalho acadêmico e existe para o avaliador conseguir entrar. Num sistema
  real, seed com credencial versionada não passa.
- **Sessão em memória.** Reiniciar o servidor desloga todo mundo.
- **Sem recuperação de senha.** Fora do escopo da PoC.
