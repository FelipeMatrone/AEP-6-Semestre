# API HTTP — `/api/tarefas`

Contrato da API da 1ª entrega. O Springdoc já expõe o contrato vivo em
[`/docs`](http://localhost:8080/docs) (OpenAPI cru em `/v3/api-docs`); este
arquivo existe para quem lê o repositório **sem subir a aplicação** — que é
exatamente a situação de quem corrige.

Base: `http://localhost:8080`. Tudo em `application/json`, UTF-8. Sem
autenticação nesta entrega.

---

## Operações

| Método | Caminho | Entrada | Sucesso | Recurso ausente |
|---|---|---|---|---|
| `GET` | `/api/tarefas` | — | `200` + lista resumida | — (lista vazia) |
| `GET` | `/api/tarefas/{id}` | — | `200` + tarefa completa | `404` |
| `POST` | `/api/tarefas` | `TarefaCreateRequest` | `201` + header `Location` + tarefa completa | — |
| `PUT` | `/api/tarefas/{id}` | `TarefaUpdateRequest` | `200` + tarefa completa | `404` |
| `DELETE` | `/api/tarefas/{id}` | — | `204`, sem corpo | `404` |

Corpo inválido em `POST` e `PUT` devolve `400` com o mapa `fieldErrors`.

## Duas representações de saída, de propósito

A listagem **não** devolve o mesmo objeto que a busca individual. É projeção
deliberada, não esquecimento.

| Campo | `GET /api/tarefas` (resumo) | `GET /api/tarefas/{id}` (completo) |
|---|---|---|
| `id` | ✔ | ✔ |
| `titulo` | ✔ | ✔ |
| `prazo` | ✔ | ✔ |
| `prioridade` | ✔ | ✔ |
| `observacao` | ✔ | ✔ |
| `concluida` | ✔ | ✔ |
| `criadaEm` | — | ✔ |
| `atualizadaEm` | — | ✔ |

O resumo omite só os dois carimbos de tempo. Consequência prática: **todos os
campos editáveis cabem no resumo**, então um cliente consegue montar o `PUT`
direto da linha da lista, sem um `GET` por id antes. O `crud.html` busca o
recurso completo de todo modo — é uma requisição a mais que mantém o cliente
correto caso a projeção volte a encolher.

## Campos

| Campo | Tipo JSON | Formato | Observação |
|---|---|---|---|
| `id` | string | hexadecimal de 24 caracteres | o `ObjectId` do MongoDB |
| `titulo` | string | livre | obrigatório, não pode ser só espaços |
| `prazo` | string | `AAAA-MM-DD` | **data de calendário**, não instante |
| `prioridade` | string | `alta` \| `media` \| `baixa` | slug ASCII, sem acento |
| `concluida` | boolean | — | nasce `false`; só o `PUT` altera |
| `observacao` | string ou `null` | livre | opcional; ausente no corpo vira `null` na resposta |
| `criadaEm` | string | ISO-8601 em UTC | definido pelo servidor |
| `atualizadaEm` | string | ISO-8601 em UTC | definido pelo servidor |

`prazo` é data de calendário e **não** deve ser convertido com fuso no cliente:
`new Date("2026-09-20")` em Maringá devolve 19 de setembro. A regra completa
está em `docs/modelagem-banco.md`, seção "Datas e fuso".

## Regras de validação

| Campo | `POST` | `PUT` |
|---|---|---|
| `titulo` | obrigatório, não vazio | obrigatório, não vazio |
| `prazo` | obrigatório | obrigatório |
| `prioridade` | obrigatório, slug válido | obrigatório, slug válido |
| `observacao` | opcional | opcional |
| `concluida` | **não aceito** | **obrigatório** |

A assimetria é regra de negócio, não descuido: **`concluida` não entra na
criação.** Tarefa nasce pendente. O campo nem existe em `TarefaCreateRequest`,
então não há como criar uma tarefa já concluída — só o `PUT` a conclui.

`observacao` ausente no corpo chega como `null` na resposta, e o banco não grava
a chave — o Spring Data omite campo nulo em vez de gravar `null`. O `$jsonSchema`
não a lista em `required`.

Campo extra enviado no corpo é ignorado. Slug de prioridade desconhecido falha
na desserialização, **antes** da validação, e por isso volta como `400` com
`fieldErrors` vazio em vez de um erro por campo.

## Exemplos

### Criar

```http
POST /api/tarefas
Content-Type: application/json

{
  "titulo": "Estudar agregações do MongoDB",
  "prazo": "2026-09-20",
  "prioridade": "alta",
  "observacao": "Revisar o capítulo 4"
}
```

```http
201 Created
Location: http://localhost:8080/api/tarefas/66e3f1a2b4c5d6e7f8a9b0c1

{
  "id": "66e3f1a2b4c5d6e7f8a9b0c1",
  "titulo": "Estudar agregações do MongoDB",
  "prazo": "2026-09-20",
  "prioridade": "alta",
  "concluida": false,
  "observacao": "Revisar o capítulo 4",
  "criadaEm": "2026-09-11T14:02:33.481Z",
  "atualizadaEm": "2026-09-11T14:02:33.481Z"
}
```

### Listar

```http
GET /api/tarefas
```

```http
200 OK

[
  {
    "id": "66e3f1a2b4c5d6e7f8a9b0c1",
    "titulo": "Estudar agregações do MongoDB",
    "prazo": "2026-09-20",
    "prioridade": "alta",
    "observacao": "Revisar o capítulo 4",
    "concluida": false
  }
]
```

### Atualizar

```http
PUT /api/tarefas/66e3f1a2b4c5d6e7f8a9b0c1
Content-Type: application/json

{
  "titulo": "Estudar agregações do MongoDB",
  "prazo": "2026-09-21",
  "prioridade": "baixa",
  "observacao": "Revisar também os índices",
  "concluida": true
}
```

```http
200 OK

{
  "id": "66e3f1a2b4c5d6e7f8a9b0c1",
  "titulo": "Estudar agregações do MongoDB",
  "prazo": "2026-09-21",
  "prioridade": "baixa",
  "concluida": true,
  "observacao": "Revisar também os índices",
  "criadaEm": "2026-09-11T14:02:33.481Z",
  "atualizadaEm": "2026-09-11T14:09:57.112Z"
}
```

`criadaEm` não muda e `atualizadaEm` avança — os dois são definidos pelo
servidor e não entram em `TarefaUpdateRequest`, então omiti-los não os apaga.

### Excluir

```http
DELETE /api/tarefas/66e3f1a2b4c5d6e7f8a9b0c1
```

```http
204 No Content
```

## Formato do erro

Toda falha devolve o mesmo envelope, vindo do
`@RestControllerAdvice`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Corpo da requisição inválido ou mal formado",
  "path": "/api/tarefas",
  "fieldErrors": {
    "titulo": "O título é obrigatório",
    "prazo": "O prazo é obrigatório"
  }
}
```

`fieldErrors` vem **vazio (`{}`), nunca nulo**, quando a falha não é de
validação de campo — o cliente pode iterar sem checar.

| Situação | `status` | `fieldErrors` |
|---|---|---|
| campo obrigatório ausente ou em branco | `400` | um par por campo |
| JSON mal formado | `400` | `{}` |
| slug de prioridade desconhecido | `400` | `{}` |
| id inexistente em `GET`/`PUT`/`DELETE` | `404` | `{}` |

Exemplo de `404`:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Tarefa não encontrada: 66e3f1a2b4c5d6e7f8a9b0c1",
  "path": "/api/tarefas/66e3f1a2b4c5d6e7f8a9b0c1",
  "fieldErrors": {}
}
```

## Experimentar pela linha de comando

```bash
# criar
curl -i -X POST http://localhost:8080/api/tarefas \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Estudar NoSQL","prazo":"2026-09-20","prioridade":"alta","observacao":"Revisar o capítulo 4"}'

# listar
curl http://localhost:8080/api/tarefas

# ver o 400 com fieldErrors
curl -i -X POST http://localhost:8080/api/tarefas \
  -H 'Content-Type: application/json' -d '{}'
```

## Limites desta versão

Declarados aqui para não virarem pergunta na correção:

- **Sem autenticação e sem dono.** A tarefa não tem campo de usuário nesta
  entrega: a coleção `usuarios` é a 2ª entrega, e criá-la agora violaria o teto
  de uma coleção só.
- **Sem escopo por usuário.** `GET /api/tarefas` devolve a coleção inteira. O
  filtro por `usuarioId` entra junto com a autenticação real.
- **Sem paginação e sem ordenação.** A listagem devolve tudo, na ordem do banco.
- **Sem `PATCH`.** `PUT` exige o recurso completo; atualização parcial não faz
  parte do CRUD básico da seção 8 do enunciado.
- **Última escrita vence.** Duas edições simultâneas se sobrescrevem.
