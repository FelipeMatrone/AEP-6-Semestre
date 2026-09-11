// Executado pela imagem oficial do Mongo na PRIMEIRA subida do container,
// contra o banco definido em MONGO_INITDB_DATABASE. Para rodar de novo depois
// de mudar este arquivo: docker compose down -v (o -v apaga o volume).
//
// Especificação e justificativas: docs/modelagem-banco.md

const agora = new Date();

db.createCollection("tarefas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      // observacao fora da lista: campo opcional. O Spring Data omite a chave
      // por completo quando o valor e null, em vez de grava-la como null.
      required: [
        "titulo",
        "prazo",
        "prioridade",
        "concluida",
        "criadaEm",
        "atualizadaEm"
      ],
      additionalProperties: false,
      properties: {
        // _id precisa estar declarado: com additionalProperties: false ele
        // seria "campo extra" e todo insert falharia
        _id: { bsonType: "objectId" },
        titulo: { bsonType: "string" },
        // data de calendário, sempre à meia-noite UTC — a aplicação fixa o fuso
        // do processo em UTC para que a invariante valha em qualquer máquina
        prazo: { bsonType: "date" },
        // o mesmo slug ASCII que a API expõe; o rótulo acentuado é da tela
        prioridade: { bsonType: "string", enum: ["alta", "media", "baixa"] },
        concluida: { bsonType: "bool" },
        observacao: { bsonType: "string" },
        criadaEm: { bsonType: "date" },
        atualizadaEm: { bsonType: "date" }
      }
    }
  }
});

// Dados de demonstração: o suficiente para a listagem não abrir vazia na
// apresentação, com uma tarefa concluída e duas pendentes.
db.tarefas.insertMany([
  {
    titulo: "Entregar a modelagem do banco",
    prazo: ISODate("2026-09-08T00:00:00Z"),
    prioridade: "alta",
    concluida: true,
    observacao: "Revisar os requisitos antes da entrega.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    titulo: "Definir o ODS do projeto",
    prazo: ISODate("2026-09-12T00:00:00Z"),
    prioridade: "media",
    concluida: false,
    observacao: "Validar a escolha com a equipe.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    titulo: "Gravar o vídeo de demonstração",
    prazo: ISODate("2026-09-19T00:00:00Z"),
    prioridade: "baixa",
    concluida: false,
    observacao: "Preparar o roteiro antes de gravar.",
    criadaEm: agora,
    atualizadaEm: agora
  }
]);
