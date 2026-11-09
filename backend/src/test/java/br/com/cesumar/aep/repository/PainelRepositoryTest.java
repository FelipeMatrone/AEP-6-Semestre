package br.com.cesumar.aep.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

// sem Mongo: afirma sobre os estágios do pipeline montado.
class PainelRepositoryTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 8);

	private final PainelRepository repository = new PainelRepository(null);

	@Test
	void pipelineFiltraPeloUsuarioNoPrimeiroEstagio() {
		List<Document> estagios = pipeline();

		assertThat(estagios.get(0).get("$match", Document.class).getString("usuarioId")).isEqualTo("u1");
	}

	@Test
	void pipelineUsaLookupEntreCategoriasETarefas() {
		Document lookup = pipeline().get(1).get("$lookup", Document.class);

		assertThat(lookup.getString("from")).isEqualTo("tarefas");
		assertThat(lookup.getString("as")).isEqualTo("tarefas");
		// a forma de quatro argumentos não tem estes campos e devolveria vazio
		assertThat(lookup).doesNotContainKeys("localField", "foreignField");
		assertThat(lookup.get("let", Document.class).getString("catId")).isEqualTo("$_id");
		assertThat(lookup.get("pipeline", List.class).get(0).toString())
				.contains("$expr", "$toString", "$$catId", "$categoriaId");
	}

	private List<Document> pipeline() {
		Aggregation aggregation = repository.montarPipeline("u1", HOJE);

		return aggregation.toPipeline(Aggregation.DEFAULT_CONTEXT);
	}

}
