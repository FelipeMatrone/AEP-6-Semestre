package br.com.cesumar.aep.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;

import br.com.cesumar.aep.repository.TarefaRepository;

// O que este teste prova: o CRUD completo vai e volta contra um MongoDB real,
// com os conversores de prioridade e o _class desligado em operacao.
// O que ele NAO prova: o contrainer sobe um Mongo limpo, sem executar
// banco/init.js, logo sem o $jsonSchema e sem os indices do banco de
// desenvolvimento. A conformidade com aquele validador e verificada a mao
// (T24 em docs/backend-tarefas.md), e o fuso do processo nao e fixado em UTC
// aqui porque @SpringBootTest nao passa pelo main de AepApplication.
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TarefaApiIT {

	private static final String BASE_PATH = "/api/tarefas";

	// a mesma imagem do docker-compose.yml: passar contra uma versao diferente
	// da que o projeto usa nao prova o que o teste promete provar.
	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

	@DynamicPropertySource
	static void apontarParaOContainer(DynamicPropertyRegistry registro) {
		registro.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TarefaRepository repository;

	@BeforeEach
	void limparColecao() {
		repository.deleteAll();
	}

	@Test
	void deveExecutarOCicloDeVidaDaTarefa() throws Exception {
		String corpoCriado = mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "Revisar a modelagem",
						  "prazo": "2026-09-20",
						  "prioridade": "alta",
						  "observacao": "Conferir o capitulo de agregacoes"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists(HttpHeaders.LOCATION))
				.andExpect(jsonPath("$.concluida").value(false))
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		String id = JsonPath.read(corpoCriado, "$.id");

		mockMvc.perform(get(BASE_PATH + "/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Revisar a modelagem"))
				.andExpect(jsonPath("$.prazo").value("2026-09-20"))
				.andExpect(jsonPath("$.prioridade").value("alta"))
				.andExpect(jsonPath("$.observacao").value("Conferir o capitulo de agregacoes"));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(id));

		mockMvc.perform(put(BASE_PATH + "/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "Revisar a modelagem do banco",
						  "prazo": "2026-09-21",
						  "prioridade": "baixa",
						  "observacao": "Conferir tambem os indices",
						  "concluida": true
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.titulo").value("Revisar a modelagem do banco"))
				.andExpect(jsonPath("$.prioridade").value("baixa"))
				.andExpect(jsonPath("$.concluida").value(true))
				.andExpect(jsonPath("$.observacao").value("Conferir tambem os indices"))
				// criadaEm nao esta em TarefaUpdateRequest: o update preserva o que
				// foi persistido em vez de apaga-lo por omissao.
				.andExpect(jsonPath("$.criadaEm").isNotEmpty());

		mockMvc.perform(delete(BASE_PATH + "/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(BASE_PATH + "/{id}", id))
				.andExpect(status().isNotFound());
	}

}
