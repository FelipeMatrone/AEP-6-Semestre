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

## Decisão: coleção única `usuarios`, como agregado

`Usuario` é **raiz de agregado**: as tarefas vivem dentro do documento do dono e
nunca são acessadas por fora. Toda operação de tarefa entra pelo usuário, que é
quem valida.

Consequências, em ordem de importância:

1. **Login e cadastro persistem de verdade** na primeira entrega. `senhaHash`
   fica em um lugar só, sem duplicação e sem anomalia de atualização.
2. **Carregar e salvar o documento inteiro basta.** A fronteira do agregado
   coincide com a fronteira do documento, então a atomicidade de escrita de um
   único documento — que o MongoDB garante — cobre toda operação. Sem operador
   posicional, sem `arrayFilters`.
3. **Objetos com comportamento, não CRUD anêmico.** O usuário é quem sabe
   concluir, editar e remover uma tarefa sua. Isso atende o critério de
   programação orientada a objetos melhor do que repositórios rasos atenderiam.

### Alternativas descartadas

| Alternativa | Por que caiu |
|---|---|
| `usuarios` + `tarefas` separadas | Duas coleções e um relacionamento: é o enunciado da segunda entrega. Risco direto no critério de banco. |
| Coleção única `tarefas`, usuário embutido em cada uma | `senhaHash` duplicado em todo documento — trocar senha vira `updateMany`. Pior: usuário recém-cadastrado, sem tarefas, não existe em lugar nenhum para autenticar. |
| Coleção única `tarefas`, login fora do banco | Cumpre a regra, mas cadastro não persiste. Descartada por escolha do grupo. |
| Coleção genérica com campo `tipo` | Quebra "objetos homogêneos" de forma explícita. |

> **Se o professor confirmar** que autenticação conta como infraestrutura e não
> entra na contagem de coleções, `tarefas` sai do array e vira coleção própria com
> `usuarioId`. A mudança é aditiva: extrai, não reescreve.

## Especificação do documento

Coleção: **`usuarios`**

### Raiz

| Campo | Tipo BSON | Regra |
|---|---|---|
| `_id` | ObjectId | Gerado pelo MongoDB. |
| `nome` | String | Obrigatório, sem espaços nas pontas. |
| `email` | String | Obrigatório, **único**, normalizado em minúsculas antes de gravar. |
| `senhaHash` | String | Obrigatório. Hash BCrypt. Nunca a senha em claro, nunca devolvido por API. |
| `criadoEm` | Date | Instante do cadastro. |
| `tarefas` | Array de subdocumentos | Obrigatório. Usuário sem tarefas guarda array **vazio**, nunca `null` nem campo ausente. |

### Subdocumento de `tarefas[]`

| Campo | Tipo BSON | Regra |
|---|---|---|
| `id` | String | UUID gerado pela aplicação. Único dentro do array — é por ele que a tarefa é endereçada. |
| `titulo` | String | Obrigatório, sem espaços nas pontas. |
| `prazo` | Date | Data de calendário, gravada à meia-noite UTC. Ver "Datas e fuso". |
| `prioridade` | String | `"alta"`, `"media"` ou `"baixa"`. Slug ASCII, sem acento. |
| `concluida` | Boolean | Criada como `false`. |
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

Fechar o schema nos dois níveis transforma esse typo em `Document failed
validation` na hora da escrita.

O preço é uma exigência que surpreende: **`_id` precisa ser declarado em
`properties`**. Com o schema fechado, o `_id` que o MongoDB gera sozinho passa a
contar como campo extra, e todo `insertOne` falharia.

> O schema, o índice e a carga de exemplo estão em `banco/init.js`. A tabela
> acima é a especificação contra a qual esse arquivo foi escrito.

## Índices

Um só:

- `{ email: 1 }`, **único** — sustenta o login e impede cadastro duplicado no
  banco, não só na aplicação.

Nada é indexado dentro de `tarefas`: elas nunca são consultadas fora do documento
do dono. Índice que ninguém usa é escrita mais lenta de graça.

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
| `id: 1, 2, 3` | vira ObjectId / UUID | Inteiro sequencial exige contador central. |

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

Não há detecção de fuso em lugar nenhum, e não deve haver: **o navegador já é o
fuso do usuário**. A pergunta "é para hoje?" depende de quem está lendo, então é
derivada na leitura, junto com "pendentes" e as contagens do painel.

**Teto aceito:** filtrar na tela pressupõe que as tarefas do usuário cabem em uma
resposta só. Para a PoC cabem com folga. Se um dia não couberem, o filtro volta
para o servidor — e aí com fuso explícito, assumido conscientemente.

## Limites conhecidos

- **Escrita concorrente.** Ler o agregado, alterar e salvar inteiro faz duas abas
  do mesmo usuário sobrescreverem uma à outra. Aceito: a PoC tem um usuário. Se
  virar problema, bloqueio otimista por versão resolve sem mexer na modelagem.
- **Crescimento do array.** O limite de 16 MB por documento equivale a dezenas de
  milhares de tarefas por usuário — não é restrição real aqui. Mas é a pergunta
  certa sempre que se aninha um array que cresce, e a resposta é o que justifica
  aninhar neste caso.

## Caminho para a 2ª entrega

A segunda entrega pede múltiplas coleções, relacionamento entre elas e pelo menos
uma coleção com objetos complexos. A evolução é aditiva:

1. `tarefas` sai do array e vira coleção própria, referenciando `usuarioId`.
2. `categorias` entra como coleção nova, referenciando `usuarioId`, com a lista de
   anotações **aninhada** — o objeto complexo exigido, e a tela de Anotações, que
   está preservada justamente para isso.
3. `usuarios` fica só com identidade e credencial.

Resultado: três coleções, dois relacionamentos, um objeto complexo novo. A
evolução fica visível em diff e demonstrável em vídeo.

## Em aberto

- **ODS não definido.** Vale 0,1 no critério "Problema e alinhamento ao ODS" e é
  pré-requisito do README e do vídeo. Não afeta a modelagem acima, mas trava a
  entrega. Decisão do grupo — ver `sugestoes-projetos.md`.
- **Interpretação da seção 8** ainda não confirmada com o professor. A modelagem
  acima adota a leitura mais restritiva de propósito: se ele for rígido, a entrega
  está coberta; se for flexível, nada se perde.
