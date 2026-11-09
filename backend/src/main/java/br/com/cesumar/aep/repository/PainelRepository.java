package br.com.cesumar.aep.repository;

import java.time.LocalDate;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.model.Tarefa;

@Repository
public class PainelRepository {

	private final MongoTemplate mongoTemplate;

	public PainelRepository(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	// categoria com as tarefas que apontam para ela. As contas (atrasada,
	// percentual, balde sem disciplina) são do PainelService.
	public record CategoriaComTarefas(String id, String nome, Cor cor, List<Tarefa> tarefas) {
	}

	public List<CategoriaComTarefas> agregar(String usuarioId, LocalDate hoje) {
		return mongoTemplate.aggregate(montarPipeline(usuarioId, hoje), "categorias", CategoriaComTarefas.class)
				.getMappedResults();
	}

	// `hoje` ainda não entra em estágio nenhum: atrasada é decidida no Service,
	// onde a fronteira do "<" é testável sem Mongo. O parâmetro fica na
	// assinatura para o dia ser sempre injetado, nunca lido aqui dentro.
	public Aggregation montarPipeline(String usuarioId, LocalDate hoje) {
		// as referências entre documentos são string e o _id é ObjectId: o
		// $lookup de quatro argumentos compara tipos diferentes, não dá erro e
		// devolve vazio para toda categoria. Daí let + $expr + $toString.
		Document lookup = Document.parse("""
				{ $lookup: { from: "tarefas", let: { catId: "$_id" },
				    pipeline: [ { $match: { $expr: { $eq: ["$categoriaId", { $toString: "$$catId" }] } } } ],
				    as: "tarefas" } }""");

		return Aggregation.newAggregation(
				Aggregation.match(Criteria.where("usuarioId").is(usuarioId)),
				Aggregation.stage(lookup));
	}

}
