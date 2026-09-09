# Modelagem do banco — 1ª entrega

Modelagem do MongoDB para a primeira entrega da AEP. Decisões, especificação dos
campos e o caminho para a segunda entrega.

Referência normativa: `AEP_ESoft_6S.md`, seção 8.

---

## A restrição que define tudo

A seção 8 limita a primeira entrega a:

- **uma única coleção**
- **objetos homogêneos, com estrutura simples**
- **operações básicas de CRUD**

Isso não é conselho de simplicidade, é teto. As telas atuais têm quatro entidades
naturais — usuário, tarefa, categoria e anotação. Qualquer modelagem que cubra
todas ou usa múltiplas coleções ou aninha subdocumentos, e **as duas coisas são o
enunciado da segunda entrega**. Modelar tudo agora não adianta trabalho: gasta o
critério "evolução verificável" do segundo bimestre.

A modelagem abaixo é a resposta a esse teto, não a modelagem que o produto teria
se não houvesse teto.

## Decisão: coleção única `tarefas`, com documento achatado

A coleção é `tarefas`, cada documento é uma tarefa, e o dono é um **campo
escalar** (`dono`) dentro dela. Nenhum array de subdocumentos, nenhum aninhamento.

O que sustenta a escolha:

1. **"Estrutura simples" lido de forma literal.** O projeto de referência do
   professor modela `Linguagem` com quatro campos escalares e zero aninhamento.
   Array de subdocumentos é o exemplo que a própria seção 8 usa para descrever a
   **segunda** entrega — usá-lo agora entrega de graça o critério que vale nota
   depois.
2. **O CRUD é o da entidade avaliada.** `GET /api/tarefas` lê a coleção inteira,
   `POST` insere um documento, `PUT` e `DELETE` endereçam um `_id`. Cada operação
   HTTP é uma operação de coleção, sem operador posicional e sem `arrayFilters`.
3. **A evolução fica aditiva e visível em diff.** `dono` vira `usuarioId` quando a
   coleção `usuarios` existir. Extrai, não reescreve.

O preço, aceito conscientemente: **login e cadastro continuam mock nesta
entrega**. Não há `senhaHash` no banco, porque não há coleção de usuários — e
criá-la seria a segunda coleção que o teto proíbe.

### Alternativas descartadas

| Alternativa | Por que caiu |
|---|---|
| Coleção única `usuarios`, tarefas aninhadas no agregado | Persistiria o login já nesta entrega, mas o array de subdocumentos é exatamente o "objeto complexo" da 2ª entrega. Gasta o critério de evolução e contraria a leitura literal de "estrutura simples". Era a modelagem descrita aqui até 09/09/2026. |
| `usuarios` + `tarefas` separadas | Duas coleções e um relacionamento: é o enunciado da segunda entrega. Risco direto no critério de banco. |
| Coleção única `tarefas`, usuário embutido em cada documento | `senhaHash` duplicado em todo documento — trocar senha vira `updateMany`. Pior: usuário recém-cadastrado, sem tarefas, não existe em lugar nenhum para autenticar. |
| Coleção genérica com campo `tipo` | Quebra "objetos homogêneos" de forma explícita. |

## Especificação do documento

Coleção: **`tarefas`**

| Campo | Tipo BSON | Regra |
|---|---|---|
| `_id` | ObjectId | Gerado pelo MongoDB. A aplicação o expõe como `String` no JSON. |
| `titulo` | String | Obrigatório, sem espaços nas pontas. |
| `prazo` | Date | Data de calendário, gravada à meia-noite UTC. Ver "Datas e fuso". |
| `prioridade` | String | `"alta"`, `"media"` ou `"baixa"`. Slug ASCII, sem acento. |
| `concluida` | Boolean | Criada como `false` — regra de negócio, não entrada do cliente. |
| `dono` | String | Obrigatório. Identifica de quem é a tarefa enquanto não há coleção de usuários. |
| `criadaEm` | Date | Instante da criação. |
| `atualizadaEm` | Date | Igual a `criadaEm` na criação; atualizado a cada edição. |

**Todo documento tem todos os campos.** Ausência e `null` não são estados válidos
— é o que "objetos homogêneos" quer dizer, e é o que permite ler o documento sem
verificação defensiva em cada campo.

### `additionalProperties: false`

Por padrão o `$jsonSchema` aceita campo que não está em `properties` — o que
contradiz a homogeneidade prometida acima. Um `updateOne` com o nome do campo
errado não falha: ele **cria** o campo, calado, e o documento passa a divergir
dos outros. Aconteceu durante a montagem, com um `atualizadoEm` que nasceu na
raiz enquanto o `atualizadaEm` da tarefa seguia intocado.

Fechar o schema transforma esse typo em `Document failed validation` na hora da
escrita. O preço são duas exigências que surpreendem, e as duas custaram tempo:

- **`_id` precisa ser declarado em `properties`.** Com o schema fechado, o `_id`
  que o MongoDB gera sozinho passa a contar como campo extra, e todo `insertOne`
  falharia.
- **O Spring Data grava um campo `_class`** com o nome da classe Java, que conta
  como campo extra pela mesma mecânica. Desligado em
  `configuration/MongoConfiguration`, com `DefaultMongoTypeMapper(null)` — a
  alternativa, declarar `_class` no schema, sujaria a especificação do banco com
  um detalhe de implementação do framework.

Na mesma família: o Spring Data persiste `enum` pelo `Enum.name()`, o que gravaria
`"BAIXA"` e falharia contra o `enum` do schema. Resolvido com um par de
conversores registrados no mesmo arquivo — o Java continua com `ALTA`, `MEDIA` e
`BAIXA`, o banco continua com o slug.

> O schema, o índice e a carga de exemplo estão em `banco/init.js`. A tabela
> acima é a especificação contra a qual esse arquivo foi escrito.

### Conformidade verificada

Em 09/09/2026, contra um `mongod` local com o `banco/init.js` aplicado à mão:
`POST` e `PUT` pela API gravaram documentos que **passam** pelo `$jsonSchema`
fechado, sem `_class`, com `prioridade` em slug e `prazo` exatamente em
`00:00:00.000Z`.

O que isso **não** cobre: a subida pelo `docker-compose.yml`, que é o caminho que
o professor vai usar. Essa verificação continua pendente (T24 em
`backend-tarefas.md`), porque exige Docker e a máquina onde o backend foi escrito
está com o daemon desligado.

## Índices

Um só:

- `{ dono: 1 }`, **não único** — sustenta a listagem por dono, que é o único
  filtro da primeira entrega. A mesma pessoa tem várias tarefas.

Nada mais é indexado. Índice que ninguém usa é escrita mais lenta de graça.

## Como subir o banco

```bash
docker compose up -d
```

A imagem oficial do Mongo executa tudo que estiver em
`/docker-entrypoint-initdb.d/` na **primeira** subida do container — é onde a
pasta `banco/` está montada. `MONGO_INITDB_DATABASE` define contra qual banco os
scripts rodam.

Duas consequências que economizam meia hora de confusão:

- **A pasta só é lida uma vez.** Mudou o `init.js`? `docker compose down -v` e
  sobe de novo. Sem o `-v` o volume sobrevive, o banco não está mais vazio, e o
  script é ignorado em silêncio.
- **Só `.js` e `.sh` são executados.** Um seed em `.json` exigiria um `.sh`
  chamando `mongoimport` — dois arquivos para o trabalho de um.

A imagem está fixada em `mongo:7.0`, e não em `mongo:8`: a tag flutuante muda de
conteúdo sem aviso, e a 8.0 recusa iniciar em hosts com kernel Linux 6.19 ou mais
novo — o que quebraria o Compose justamente na máquina de quem corrige.

A porta está publicada em `127.0.0.1:27017`, não em `0.0.0.0`: o banco não fica
exposto na rede. Se já houver um mongod local nessa porta, o container não sobe —
pare o serviço local ou troque a porta do lado do host.

## O que não se guarda

Os dados mockados no frontend misturam dado e apresentação. A separação é regra
de modelagem, não preferência:

| Campo no mock | Decisão | Motivo |
|---|---|---|
| `prazo: "Hoje"`, `"Em 3 dias"` | vira `Date` | `"Hoje"` fica errado amanhã. É a data formatada, não a data. |
| `paraHoje: true` | não existe | Derivado de `prazo == hoje`. Guardado, precisaria de alguém para corrigir todo dia à meia-noite. |
| `atualizadaEm: "Editado ontem"` | vira `Date` | Mesma coisa, e ainda congela o idioma dentro do banco. |
| `anotacoes: 12` (categoria) | não existe | Contagem derivada — e que já mente hoje: diz 12, existem 3. |
| `prioridade: "Média"` | vira `"media"` | Acento no banco propaga risco de normalização Unicode. Rótulo acentuado é da tela. |
| `id: 1, 2, 3` | vira ObjectId | Inteiro sequencial exige contador central. |

**Campo derivado guardado é campo que vai mentir.** Guarde o fato, derive a
pergunta. Só quebre isso quando a derivação ficar cara o bastante para ser medida.

## Datas e fuso

BSON `Date` é milissegundos desde a epoch: **não existe fuso guardado**. O driver
materializa em UTC porque é a única referência que ele tem.

Isso cria uma armadilha concreta. `prazo` é uma data de calendário — "dia 3" —
mas está gravada como o instante `2026-09-03T00:00:00Z`. Lida com acessor local
em Maringá, essa data devolve **2 de setembro**, porque meia-noite UTC é 21h do
dia anterior no Brasil. A lista de "tarefas de hoje" sai errada todo dia entre
21h e a meia-noite — janela que nenhum teste rodado em horário comercial pega.

A regra que resolve: **o acessor segue o significado do valor.**

- `prazo` é data de calendário → sempre acessores **UTC**.
- "Hoje" é um instante vivido por alguém → sempre acessores **locais**.

Cada lado vira `"AAAA-MM-DD"` e a comparação é entre strings.

Do lado do servidor a invariante não se sustenta sozinha: o conversor padrão do
Spring Data usa `ZoneId.systemDefault()`, que gravaria `03:00Z` em Maringá e o dia
seguinte em qualquer fuso positivo. Por isso `AepApplication` fixa o fuso do
processo em UTC antes de subir o Spring — uma linha, e "meia-noite UTC" passa a
ser verdade em qualquer máquina.

Não há detecção de fuso em lugar nenhum, e não deve haver: **o navegador já é o
fuso do usuário**. A pergunta "é para hoje?" depende de quem está lendo, então é
derivada na leitura, junto com "pendentes" e as contagens do painel.

**Teto aceito:** filtrar na tela pressupõe que as tarefas do usuário cabem em uma
resposta só. Para a PoC cabem com folga. Se um dia não couberem, o filtro volta
para o servidor — e aí com fuso explícito, assumido conscientemente.

## Limites conhecidos

- **`dono` é uma string livre.** Sem coleção de usuários, nada impede duas grafias
  do mesmo nome, e nada garante que quem chama a API é quem diz ser. Aceito: a
  autenticação é mock nesta entrega e a API não é publicada fora da máquina.
- **Sem escopo por usuário na listagem.** `GET /api/tarefas` devolve a coleção
  inteira. O filtro por `dono` tem índice e entra junto com a autenticação real,
  na segunda entrega.
- **Última escrita vence.** Duas edições simultâneas da mesma tarefa se
  sobrescrevem. Aceito: a PoC tem um usuário. Bloqueio otimista por versão resolve
  sem mexer na modelagem.

## Caminho para a 2ª entrega

A segunda entrega pede múltiplas coleções, relacionamento entre elas e pelo menos
uma coleção com objetos complexos. A evolução é aditiva:

1. `usuarios` entra como coleção nova, com `nome`, `email` único e `senhaHash` —
   e o login e o cadastro deixam de ser mock.
2. `dono` vira `usuarioId`, referenciando essa coleção.
3. `categorias` entra referenciando `usuarioId`, com a lista de anotações
   **aninhada** — o objeto complexo exigido, e a tela de Anotações, que está
   preservada justamente para isso.

Resultado: três coleções, dois relacionamentos, um objeto complexo novo. A
evolução fica visível em diff e demonstrável em vídeo.

## Em aberto

- **ODS não definido.** Vale 0,1 no critério "Problema e alinhamento ao ODS" e é
  pré-requisito do README e do vídeo. Não afeta a modelagem acima, mas trava a
  entrega. Decisão do grupo — ver `sugestoes-projetos.md`.
- **Interpretação da seção 8** ainda não confirmada com o professor. A modelagem
  acima adota a leitura mais restritiva de propósito: se ele for rígido, a entrega
  está coberta; se for flexível, nada se perde.
