package br.com.cesumar.aep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

	private static final String BASE_PATH = "/api/categorias";

	private static final String CATEGORIA_ID = "672f0000000000000000b001";

	private static final String ANOTACAO_ID = "672f0000000000000000c001";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listarDevolveCategoriasComTotalDeAnotacoes() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(CATEGORIA_ID))
				.andExpect(jsonPath("$[0].nome").isNotEmpty())
				.andExpect(jsonPath("$[0].cor").value("roxo"))
				.andExpect(jsonPath("$[0].totalAnotacoes").isNumber())
				// a listagem é resumo: a lista de anotações só vem no detalhe
				.andExpect(jsonPath("$[0].anotacoes").doesNotExist());
	}

	@Test
	void buscarPorIdDevolveAnotacoesAninhadas() throws Exception {
		mockMvc.perform(get(BASE_PATH + "/{id}", CATEGORIA_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(CATEGORIA_ID))
				.andExpect(jsonPath("$.anotacoes[0].id").value(ANOTACAO_ID))
				.andExpect(jsonPath("$.anotacoes[0].titulo").isNotEmpty())
				.andExpect(jsonPath("$.anotacoes[0].conteudo").isNotEmpty())
				.andExpect(jsonPath("$.anotacoes[0].atualizadaEm").isNotEmpty())
				.andExpect(jsonPath("$.totalAnotacoes").doesNotExist());
	}

	@Test
	void criarDevolve201ComLocation() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("Banco de Dados", "magenta")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/categorias/" + CATEGORIA_ID))
				.andExpect(jsonPath("$.nome").value("Banco de Dados"))
				.andExpect(jsonPath("$.cor").value("magenta"));
	}

	@Test
	void criarSemNomeDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("  ", "roxo")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.nome").value("O nome é obrigatório"));
	}

	@Test
	void criarComCorInvalidaDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("Banco de Dados", "rosa")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void atualizarDevolveCategoriaAtualizada() throws Exception {
		mockMvc.perform(put(BASE_PATH + "/{id}", CATEGORIA_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("Redes", "verde")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(CATEGORIA_ID))
				.andExpect(jsonPath("$.nome").value("Redes"))
				.andExpect(jsonPath("$.cor").value("verde"));
	}

	@Test
	void excluirDevolve204() throws Exception {
		mockMvc.perform(delete(BASE_PATH + "/{id}", CATEGORIA_ID))
				.andExpect(status().isNoContent());
	}

	@Test
	void criarAnotacaoDevolve201ComLocation() throws Exception {
		mockMvc.perform(post(BASE_PATH + "/{id}/anotacoes", CATEGORIA_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoAnotacao("Resumo", "<div>texto</div>")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location",
						"http://localhost/api/categorias/" + CATEGORIA_ID + "/anotacoes/" + ANOTACAO_ID))
				.andExpect(jsonPath("$.titulo").value("Resumo"))
				.andExpect(jsonPath("$.conteudo").value("<div>texto</div>"));
	}

	@Test
	void criarAnotacaoSemTituloDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH + "/{id}/anotacoes", CATEGORIA_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoAnotacao("", "<div>texto</div>")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.titulo").value("O título é obrigatório"));
	}

	@Test
	void atualizarAnotacaoDevolveAnotacaoAtualizada() throws Exception {
		mockMvc.perform(put(BASE_PATH + "/{id}/anotacoes/{anotacaoId}", CATEGORIA_ID, ANOTACAO_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoAnotacao("Novo título", "<div>novo</div>")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ANOTACAO_ID))
				.andExpect(jsonPath("$.titulo").value("Novo título"));
	}

	@Test
	void excluirAnotacaoDevolve204() throws Exception {
		mockMvc.perform(delete(BASE_PATH + "/{id}/anotacoes/{anotacaoId}", CATEGORIA_ID, ANOTACAO_ID))
				.andExpect(status().isNoContent());
	}

	private String corpo(String nome, String cor) {
		return """
				{ "nome": "%s", "descricao": "Descrição de teste", "cor": "%s" }
				""".formatted(nome, cor);
	}

	private String corpoAnotacao(String titulo, String conteudo) {
		return """
				{ "titulo": "%s", "conteudo": "%s" }
				""".formatted(titulo, conteudo);
	}

}
