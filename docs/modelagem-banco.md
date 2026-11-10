# Modelagem do banco — 1ª entrega (histórico)

> **Documento histórico da 1ª entrega.** A modelagem vigente (três coleções, com
> `usuarios`, `categorias` e `anotacoes` aninhadas) está em
> `docs/segunda-entrega.md`. As seções "A restrição que define tudo", "Decisão" e
> "Especificação do documento" abaixo descrevem a coleção única `tarefas` achatada
> e já não valem sozinhas; o resto (índices, como subir o banco, datas, limites e
> o que a 2ª entrega fez) está atualizado.

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

A coleção é `tarefas`, e cada documento é uma tarefa com uma **observação
escalar** (`observacao`). Nenhum array de subdocumentos, nenhum aninhamento.

O que sustenta a escolha:

1. **"Estrutura simples" lido de forma literal.** O projeto de referência do
   professor modela `Linguagem` com quatro campos escalares e zero aninhamento.
   Array de subdocumentos é o exemplo que a própria seção 8 usa para descrever a
   **segunda** entrega — usá-lo agora entrega de graça o critério que vale nota
   depois.
2. **O CRUD é o da entidade avaliada.** `GET /api/tarefas` lê a coleção inteira,
   `POST` insere um documento, `PUT` e `DELETE` endereçam um `_id`. Cada operação
   HTTP é uma operação de coleção, sem operador posicional e sem `arrayFilters`.
3. **A evolução fica aditiva e visível em diff.** `usuarioId` entra quando a
   coleção `usuarios` existir, sem alterar a observação da tarefa.

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

## Especificação do documento (1ª entrega, histórica)

Coleção: **`tarefas`**

| Campo | Tipo BSON | Regra |
|---|---|---|
| `_id` | ObjectId | Gerado pelo MongoDB. A aplicação o expõe como `String` no JSON. |
| `titulo` | String | Obrigatório, sem espaços nas pontas. |
| `prazo` | Date | Data de calendário, gravada à meia-noite UTC. Ver "Datas e fuso". |
| `prioridade` | String | `"alta"`, `"media"` ou `"baixa"`. Slug ASCII, sem acento. |
| `concluida` | Boolean | Criada como `false` — regra de negócio, não entrada do cliente. |
| `observacao` | String, opcional | Ausente vira `null` na API; o Spring Data omite a chave no banco quando o valor não vem no corpo. |
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

Primeiro à mão, em 09/09/2026, contra um `mongod` local: `POST` e `PUT` pela API
gravaram documentos que passam pelo `$jsonSchema` fechado, sem `_class`, com
`prioridade` em slug e `prazo` exatamente em `00:00:00.000Z`.

Na 2ª entrega a verificação virou teste. `ValidadorMongoIT` sobe um MongoDB em
contêiner (Testcontainers), roda o `banco/init.js` de verdade com `mongosh` e
prova que o validador aceita tarefa com `usuarioId` e recusa tarefa sem ele.
`TarefaApiIT` e `PainelApiIT` fazem o caminho inteiro pela API contra o mesmo
tipo de banco. Os três rodam em `./mvnw verify` e exigem Docker ligado.

## Índices

A segunda entrega tem consultas que justificam índices, o que não havia na
primeira. Os três estão em `banco/init.js`:

| Coleção | Índice | Por quê |
|---|---|---|
| `usuarios` | `{ email: 1 }`, único | o login busca por e-mail; a unicidade é regra, não convenção |
| `categorias` | `{ usuarioId: 1 }` | toda listagem de categoria é por dono |
| `tarefas` | `{ usuarioId: 1, prazo: 1 }` | listagem e calendário filtram por dono e ordenam por prazo |

O `_id` de cada coleção já tem índice próprio, criado pelo MongoDB. Índice sem
consulta que o use continua sendo escrita mais lenta de graça, por isso nenhum
outro foi criado.

## Como subir o banco

Dois caminhos, conforme a máquina:

**Com Docker** (o caminho do professor):

```bash
docker compose down -v && docker compose up -d
```

**Com Mongo nativo** (quando o Docker não está disponível, como nesta máquina),
na porta 27017, com `mongosh` no PATH:

```bash
mongosh aep --eval 'db.dropDatabase()' && mongosh aep banco/init.js
```

O `dropDatabase` é obrigatório na segunda vez: o script cria as coleções com
`createCollection`, que falha se elas já existem.

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

- **Última escrita vence.** Duas edições simultâneas do mesmo documento se
  sobrescrevem. Bloqueio otimista por versão resolve sem mexer na modelagem.
- **Excluir disciplina não é atômico.** Primeiro desvincula as tarefas
  (`categoriaId` some), depois apaga a disciplina — duas escritas, sem transação.
  A ordem garante que uma falha no meio deixe tarefa sem disciplina, nunca tarefa
  apontando para disciplina inexistente.
- **Aninhamento sem teto de quantidade.** As anotações vivem dentro da categoria,
  e o documento do MongoDB tem limite de 16 MB. O `conteudo` tem teto de 20 000
  caracteres, mas o número de anotações não; para o uso de um estudante cabe com
  folga.

## O que a 2ª entrega fez com isso

A evolução prevista aqui foi aditiva, e foi assim que aconteceu:

1. `usuarios` entrou como coleção nova, com `nome`, `email` único e `senhaHash`.
2. `usuarioId` entrou nas tarefas, obrigatório. Tipo `string`, não ObjectId: o
   model guarda `String` e o Spring Data só coage o campo `@Id`.
3. `categorias` entrou com `usuarioId` (também `string`) e a lista de anotações
   **aninhada** — o objeto complexo. A tarefa ganhou `categoriaId` opcional.

O preço do `string` aparece na agregação do painel: `$lookup` de quatro
argumentos compara `string` com `ObjectId` e devolve vazio sem erro, por isso o
estágio usa `let` + `$expr` + `$toString` (detalhe em `docs/arquitetura.md`).

O desenho completo, com os documentos de exemplo e as regras de cada coleção,
está em `docs/segunda-entrega.md`; o schema vigente, em `banco/init.js`.
