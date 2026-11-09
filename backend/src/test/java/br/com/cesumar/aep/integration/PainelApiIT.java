package br.com.cesumar.aep.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.model.Prioridade;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.CategoriaRepository;
import br.com.cesumar.aep.repository.TarefaRepository;

// O que este teste prova: o $lookup com let + $expr + $toString casa o _id
// ObjectId da categoria com o categoriaId string da tarefa contra um Mongo
// real. Um lookup de quatro argumentos passaria nos testes de unidade e
// devolveria zero aqui.
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "672f0000000000000000a001")
class PainelApiIT {

	private static final String BASE_PATH = "/api/painel";

	private static final String USUARIO = "672f0000000000000000a001";

	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

	@DynamicPropertySource
	static void apontarParaOContainer(DynamicPropertyRegistry registro) {
		registro.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TarefaRepository tarefaRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@BeforeEach
	void limparColecoes() {
		tarefaRepository.deleteAll();
		categoriaRepository.deleteAll();
	}

	@Test
	void deveAgregarProgressoPorDisciplinaEBaldeSemDisciplina() throws Exception {
		Categoria categoria = new Categoria("Engenharia de Software", null, Cor.ROXO);
		categoria.setUsuarioId(USUARIO);
		categoria = categoriaRepository.save(categoria);

		Categoria vazia = new Categoria("Banco de Dados", null, Cor.VERDE);
		vazia.setUsuarioId(USUARIO);
		categoriaRepository.save(vazia);

		tarefaRepository.save(tarefa(categoria.getId(), true, LocalDate.of(2000, 1, 1)));
		tarefaRepository.save(tarefa(categoria.getId(), false, LocalDate.of(2000, 1, 1)));
		tarefaRepository.save(tarefa(null, false, LocalDate.of(2999, 1, 1)));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pendentes").value(2))
				.andExpect(jsonPath("$.concluidas").value(1))
				.andExpect(jsonPath("$.atrasadas").value(1))
				.andExpect(jsonPath("$.progresso[?(@.nome == 'Engenharia de Software')].total").value(2))
				.andExpect(jsonPath("$.progresso[?(@.nome == 'Engenharia de Software')].percentual").value(50))
				.andExpect(jsonPath("$.progresso[?(@.nome == 'Banco de Dados')].total").value(0))
				.andExpect(jsonPath("$.progresso[?(@.nome == 'Sem disciplina')].total").value(1));
	}

	private Tarefa tarefa(String categoriaId, boolean concluida, LocalDate prazo) {
		Tarefa tarefa = new Tarefa("Tarefa", prazo, Prioridade.MEDIA, null);
		tarefa.setUsuarioId(USUARIO);
		tarefa.setCategoriaId(categoriaId);
		tarefa.setConcluida(concluida);
		return tarefa;
	}

}
