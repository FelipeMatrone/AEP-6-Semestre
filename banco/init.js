// Executado pela imagem oficial do Mongo na PRIMEIRA subida do container,
// contra o banco definido em MONGO_INITDB_DATABASE. Para rodar de novo depois
// de mudar este arquivo: docker compose down -v (o -v apaga o volume).
// Com Mongo nativo: mongosh aep --eval 'db.dropDatabase()' && mongosh aep banco/init.js
//
// Especificação e justificativas: docs/modelagem-banco.md e
// docs/segunda-entrega.md (seção "Modelagem").

const agora = new Date();

// prazos são data de calendário à meia-noite UTC, a mesma regra do backend.
// Derivados de hoje para que a tarefa "atrasada" continue atrasada no futuro.
const hoje = new Date(Date.UTC(agora.getUTCFullYear(), agora.getUTCMonth(), agora.getUTCDate()));
const emDias = (dias) => new Date(hoje.getTime() + dias * 86400000);

const USUARIO_HEX = "672f0000000000000000a001";

db.createCollection("usuarios", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nome", "email", "senhaHash", "metaSemanal", "criadoEm"],
      additionalProperties: false,
      properties: {
        // _id precisa estar declarado: com additionalProperties: false ele
        // seria "campo extra" e todo insert falharia
        _id: { bsonType: "objectId" },
        nome: { bsonType: "string" },
        // minúsculo; a unicidade é o índice abaixo, não esta regra
        email: { bsonType: "string" },
        senhaHash: { bsonType: "string" },
        metaSemanal: { bsonType: "int", minimum: 1 },
        criadoEm: { bsonType: "date" }
      }
    }
  }
});

db.createCollection("categorias", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["usuarioId", "nome", "cor", "anotacoes", "criadaEm", "atualizadaEm"],
      additionalProperties: false,
      properties: {
        _id: { bsonType: "objectId" },
        // string, não ObjectId: model/Categoria.java grava o id do dono como String
        usuarioId: { bsonType: "string" },
        nome: { bsonType: "string" },
        descricao: { bsonType: "string" },
        // slug minúsculo; o conversor de persistência grava "roxo", não "ROXO"
        cor: { bsonType: "string", enum: ["roxo", "magenta", "verde", "ambar"] },
        criadaEm: { bsonType: "date" },
        atualizadaEm: { bsonType: "date" },
        // sempre presente, pode ser vazio: a categoria nasce sem anotações
        anotacoes: {
          bsonType: "array",
          items: {
            bsonType: "object",
            // id é string gerada no Service; o Mongo não gera _id para
            // elemento de array. Sem declarar id aqui, toda categoria com
            // anotação seria rejeitada pelo validador fechado.
            required: ["id", "titulo", "conteudo", "criadaEm", "atualizadaEm"],
            additionalProperties: false,
            properties: {
              id: { bsonType: "string" },
              titulo: { bsonType: "string" },
              conteudo: { bsonType: "string" },
              criadaEm: { bsonType: "date" },
              atualizadaEm: { bsonType: "date" }
            }
          }
        }
      }
    }
  }
});

db.createCollection("tarefas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      // observacao, categoriaId e concluidaEm ficam fora da lista: campos opcionais. O
      // Spring Data omite a chave por completo quando o valor e null.
      required: [
        "usuarioId",
        "titulo",
        "prazo",
        "prioridade",
        "concluida",
        "criadaEm",
        "atualizadaEm"
      ],
      additionalProperties: false,
      properties: {
        _id: { bsonType: "objectId" },
        // string, pelo mesmo motivo de categorias.usuarioId
        usuarioId: { bsonType: "string" },
        categoriaId: { bsonType: "string" },
        titulo: { bsonType: "string" },
        // data de calendário, sempre à meia-noite UTC
        prazo: { bsonType: "date" },
        // o mesmo slug ASCII que a API expõe; o rótulo acentuado é da tela
        prioridade: { bsonType: "string", enum: ["alta", "media", "baixa"] },
        concluida: { bsonType: "bool" },
        concluidaEm: { bsonType: "date" },
        observacao: { bsonType: "string" },
        criadaEm: { bsonType: "date" },
        atualizadaEm: { bsonType: "date" }
      }
    }
  }
});

// Índices: cada um sustenta uma consulta já prevista (ver docs/segunda-entrega.md).
db.usuarios.createIndex({ email: 1 }, { unique: true });
db.categorias.createIndex({ usuarioId: 1 });
db.tarefas.createIndex({ usuarioId: 1, prazo: 1 });

// Dados de demonstração. Um usuário com quatro categorias (uma sem anotação,
// para a tela vazia aparecer na apresentação), seis anotações e oito tarefas:
// uma concluída, uma atrasada, e as que não têm categoriaId.
// Senha em claro para entrar na demo: aep-demo-2026, gravada abaixo como hash
// BCrypt de custo 10 — o mesmo encoder que o cadastro da API usa.
db.usuarios.insertOne({
  _id: ObjectId(USUARIO_HEX),
  nome: "Usuário de demonstração",
  email: "demo@aep.local",
  senhaHash: "$2a$10$VXylslVk9eCgQ5A/1ZzMWOFZMGqZlTcd6pLr.WDjOSbt6g2dAT1ne",
  metaSemanal: 5,
  criadoEm: agora
});

db.categorias.insertMany([
  {
    _id: ObjectId("672f0000000000000000b001"),
    usuarioId: USUARIO_HEX,
    nome: "Matemática",
    descricao: "Cálculo e álgebra linear",
    cor: "roxo",
    criadaEm: agora,
    atualizadaEm: agora,
    anotacoes: [
      {
        id: "672f0000000000000000c001",
        titulo: "Regras de derivação",
        conteudo: "<div>Produto, quociente e cadeia.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      },
      {
        id: "672f0000000000000000c002",
        titulo: "Determinantes",
        conteudo: "<div>Expansão por cofatores e regra de Sarrus.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      }
    ]
  },
  {
    _id: ObjectId("672f0000000000000000b002"),
    usuarioId: USUARIO_HEX,
    nome: "Programação",
    descricao: "Java e Spring Boot",
    cor: "verde",
    criadaEm: agora,
    atualizadaEm: agora,
    anotacoes: [
      {
        id: "672f0000000000000000c003",
        titulo: "Camadas da API",
        conteudo: "<div>Controller, Service, Repository e Mapper.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      },
      {
        id: "672f0000000000000000c004",
        titulo: "Injeção de dependência",
        conteudo: "<div>Construtor em vez de campo anotado.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      }
    ]
  },
  {
    _id: ObjectId("672f0000000000000000b003"),
    usuarioId: USUARIO_HEX,
    nome: "Banco de Dados",
    descricao: "Modelagem e MongoDB",
    cor: "ambar",
    criadaEm: agora,
    atualizadaEm: agora,
    anotacoes: [
      {
        id: "672f0000000000000000c005",
        titulo: "Subdocumentos",
        conteudo: "<div>Quando aninhar e quando separar em coleção.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      },
      {
        id: "672f0000000000000000c006",
        titulo: "Índice composto",
        conteudo: "<div>A ordem dos campos importa para a ordenação.</div>",
        criadaEm: agora,
        atualizadaEm: agora
      }
    ]
  },
  {
    _id: ObjectId("672f0000000000000000b004"),
    usuarioId: USUARIO_HEX,
    nome: "Inglês",
    descricao: "Sem anotações ainda",
    cor: "magenta",
    criadaEm: agora,
    atualizadaEm: agora,
    anotacoes: []
  }
]);

db.tarefas.insertMany([
  {
    _id: ObjectId("672f0000000000000000d001"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b003",
    titulo: "Entregar a modelagem do banco",
    prazo: emDias(-10),
    prioridade: "alta",
    concluida: true,
    concluidaEm: agora,
    observacao: "Revisar os requisitos antes da entrega.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    // atrasada: não concluída com prazo anterior a hoje
    _id: ObjectId("672f0000000000000000d002"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b001",
    titulo: "Revisar lista de exercícios de cálculo",
    prazo: emDias(-3),
    prioridade: "alta",
    concluida: false,
    observacao: "Itens 4 a 9 ainda sem resposta.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    _id: ObjectId("672f0000000000000000d003"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b002",
    titulo: "Implementar CRUD de categorias",
    prazo: emDias(2),
    prioridade: "alta",
    concluida: false,
    observacao: "Incluir as anotações aninhadas.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    _id: ObjectId("672f0000000000000000d004"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b003",
    titulo: "Estudar índices compostos do MongoDB",
    prazo: emDias(5),
    prioridade: "media",
    concluida: false,
    observacao: "Comparar com o plano de consulta do explain.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    // sem categoriaId e sem observacao: cobre os dois campos opcionais ausentes
    _id: ObjectId("672f0000000000000000d005"),
    usuarioId: USUARIO_HEX,
    titulo: "Comprar material para a apresentação",
    prazo: emDias(1),
    prioridade: "baixa",
    concluida: false,
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    _id: ObjectId("672f0000000000000000d006"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b002",
    titulo: "Ler sobre arquitetura em camadas",
    prazo: emDias(7),
    prioridade: "media",
    concluida: false,
    observacao: "Focar na separação entre Service e Repository.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    _id: ObjectId("672f0000000000000000d007"),
    usuarioId: USUARIO_HEX,
    categoriaId: "672f0000000000000000b004",
    titulo: "Revisar vocabulário de inglês",
    prazo: emDias(-1),
    prioridade: "baixa",
    concluida: true,
    concluidaEm: agora,
    observacao: "Unidade 4 concluída.",
    criadaEm: agora,
    atualizadaEm: agora
  },
  {
    _id: ObjectId("672f0000000000000000d008"),
    usuarioId: USUARIO_HEX,
    titulo: "Preparar a apresentação da segunda entrega",
    prazo: emDias(10),
    prioridade: "alta",
    concluida: false,
    criadaEm: agora,
    atualizadaEm: agora
  }
]);
