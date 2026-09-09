// Executado pela imagem oficial do Mongo na PRIMEIRA subida do container,
// contra o banco definido em MONGO_INITDB_DATABASE. Para rodar de novo depois
// de mudar este arquivo: docker compose down -v (o -v apaga o volume).
//
// Especificação e justificativas: docs/modelagem-banco.md

const agora = new Date();

db.createCollection("usuarios", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nome", "email", "senhaHash", "criadoEm", "tarefas"],
      additionalProperties: false,
      properties: {
        // _id precisa estar declarado: com additionalProperties: false ele
        // seria "campo extra" e todo insert falharia
        _id: { bsonType: "objectId" },
        nome: { bsonType: "string" },
        email: { bsonType: "string" },
        senhaHash: { bsonType: "string" },
        criadoEm: { bsonType: "date" },
        tarefas: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: [
              "id",
              "titulo",
              "prazo",
              "prioridade",
              "concluida",
              "criadaEm",
              "atualizadaEm"
            ],
            additionalProperties: false,
            properties: {
              id: { bsonType: "string" },
              titulo: { bsonType: "string" },
              prazo: { bsonType: "date" },
              prioridade: { bsonType: "string", enum: ["alta", "media", "baixa"] },
              concluida: { bsonType: "bool" },
              criadaEm: { bsonType: "date" },
              atualizadaEm: { bsonType: "date" }
            }
          }
        }
      }
    }
  }
});

// Sustenta o login e impede cadastro duplicado no banco, não só na aplicação.
// Criado antes da carga de propósito: assim ele valida o próprio seed abaixo.
db.usuarios.createIndex({ email: 1 }, { unique: true });

// Dados de demonstração. Os hashes são fictícios, respeitam apenas a forma do
// BCrypt ($2b$ + custo + 60 caracteres no total) — hash de verdade vem da
// aplicação, nunca deste arquivo.
db.usuarios.insertMany([
  {
    nome: "Samuel",
    email: "samuel@exemplo.com",
    senhaHash: "$2b$12$e0NRq8HcHl2Rk1vXQ9zvIu5rTgWJb1YyHFvKp3sQmZaLd7CxUeS6i",
    criadoEm: agora,
    tarefas: [
      {
        id: "3f2a7c18-9b4e-4d61-a0f3-2c85b7e94d10",
        titulo: "Entregar a modelagem do banco",
        // data de calendário: meia-noite UTC, ver "Datas e fuso" no doc
        prazo: ISODate("2026-09-08T00:00:00Z"),
        prioridade: "alta",
        concluida: false,
        criadaEm: agora,
        atualizadaEm: agora
      },
      {
        id: "b17d4e52-08cf-4a93-8e21-6f0ab3d95c47",
        titulo: "Definir o ODS do projeto",
        prazo: ISODate("2026-09-12T00:00:00Z"),
        prioridade: "media",
        concluida: false,
        criadaEm: agora,
        atualizadaEm: agora
      }
    ]
  },
  {
    // usuário recém-cadastrado: array vazio é estado válido, nunca null
    nome: "Felipe",
    email: "felipe@exemplo.com",
    senhaHash: "$2b$12$7KpQx3ZmWt5RvL9dHnEbYuC1sAoJ4fXi8gTyNqVeB6MrDzUk2SwPa",
    criadoEm: agora,
    tarefas: []
  }
]);
