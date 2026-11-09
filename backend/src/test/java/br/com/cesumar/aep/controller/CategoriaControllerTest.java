package br.com.cesumar.aep.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.mapper.CategoriaMapper;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

	private static final String BASE_PATH = "/api/categorias";

	private static final String USUARIO_ID = "672f0000000000000000a001";

	private static final String CATEGORIA_ID = "672f0000000000000000b001";

	private static final String ANOTACAO_ID = "672f0000000000000000c001";

	private static final String ID_OBJECTID = "[0-9a-f]{24}";

	private static final String INSTANTE_UTC = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z";

	private static final Instant AGORA = Instant.parse("2026-11-08T14:32:00Z");

	// o Mapper entra de verdade para que listagem e detalhe nasçam da mesma
	// categoria, como no sistema real.
	private final CategoriaMapper mapper = new CategoriaMapper();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CategoriaService service;

	@Test
	void listarDevolveCategoriasComTotalDeAnotacoes() throws Exception {
		when(service.listar(USUARIO_ID)).thenReturn(List.of(mapper.toSummaryResponse(categoria())));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", matchesPattern(ID_OBJECTID)))
				.andExpect(jsonPath("$[0].nome").isNotEmpty())
				.andExpect(jsonPath("$[0].cor").value("roxo"))
				.andExpect(jsonPath("$[0].totalAnotacoes").value(2))
				// a listagem é resumo: a lista de anotações só vem no detalhe
				.andExpect(jsonPath("$[0].anotacoes").doesNotExist());
	}

	@Test
	void buscarPorIdDevolveAnotacoesAninhadas() throws Exception {
		when(service.buscarPorId(CATEGORIA_ID, USUARIO_ID)).thenReturn(mapper.toResponse(categoria()));

		mockMvc.perform(get(BASE_PATH + "/{id}", CATEGORIA_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", matchesPattern(ID_OBJECTID)))
				.andExpect(jsonPath("$.anotacoes[0].id", matchesPattern(ID_OBJECTID)))
				.andExpect(jsonPath("$.anotacoes.length()").value(2))
				.andExpect(jsonPath("$.anotacoes[1].id", matchesPattern(ID_OBJECTID)))
				.andExpect(jsonPath("$.anotacoes[0].titulo").isNotEmpty())
				.andExpect(jsonPath("$.anotacoes[0].conteudo").isNotEmpty())
				.andExpect(jsonPath("$.anotacoes[0].criadaEm", matchesPattern(INSTANTE_UTC)))
				.andExpect(jsonPath("$.anotacoes[0].atualizadaEm", matchesPattern(INSTANTE_UTC)))
				.andExpect(jsonPath("$.totalAnotacoes").doesNotExist());
	}

	@Test
	void totalAnotacoesDaListagemCasaComAListaDoDetalhe() throws Exception {
		Categoria categoria = categoria();
		when(service.listar(USUARIO_ID)).thenReturn(List.of(mapper.toSummaryResponse(categoria)));
		when(service.buscarPorId(CATEGORIA_ID, USUARIO_ID)).thenReturn(mapper.toResponse(categoria));

		String listagem = mockMvc.perform(get(BASE_PATH)).andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(listagem, "$[0].id");
		int total = JsonPath.read(listagem, "$[0].totalAnotacoes");

		String detalhe = mockMvc.perform(get(BASE_PATH + "/{id}", id)).andReturn().getResponse().getContentAsString();
		int tamanho = JsonPath.<List<Object>>read(detalhe, "$.anotacoes").size();

		assertEquals(tamanho, total);
	}

	@Test
	void buscarPorIdInexistenteDevolve404() throws Exception {
		when(service.buscarPorId(CATEGORIA_ID, USUARIO_ID)).thenThrow(new CategoriaNotFoundException(CATEGORIA_ID));

		mockMvc.perform(get(BASE_PATH + "/{id}", CATEGORIA_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void criarDevolve201ComLocation() throws Exception {
		Categoria criada = new Categoria("Banco de Dados", "Descrição de teste", Cor.MAGENTA);
		criada.setId(CATEGORIA_ID);
		when(service.criar(any(CategoriaCreateRequest.class), eq(USUARIO_ID))).thenReturn(mapper.toResponse(criada));

		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("Banco de Dados", "magenta")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/categorias/" + CATEGORIA_ID))
				.andExpect(jsonPath("$.id", matchesPattern(ID_OBJECTID)))
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
		Categoria atualizada = new Categoria("Redes", "Descrição de teste", Cor.VERDE);
		atualizada.setId(CATEGORIA_ID);
		when(service.atualizar(eq(CATEGORIA_ID), any(CategoriaUpdateRequest.class), eq(USUARIO_ID)))
				.thenReturn(mapper.toResponse(atualizada));

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

		verify(service).excluir(CATEGORIA_ID, USUARIO_ID);
	}

	@Test
	void excluirInexistenteDevolve404() throws Exception {
		doThrow(new CategoriaNotFoundException(CATEGORIA_ID)).when(service).excluir(CATEGORIA_ID, USUARIO_ID);

		mockMvc.perform(delete(BASE_PATH + "/{id}", CATEGORIA_ID))
				.andExpect(status().isNotFound());
	}

	@Test
	void criarAnotacaoDevolve201ComLocation() throws Exception {
		mockMvc.perform(post(BASE_PATH + "/{id}/anotacoes", CATEGORIA_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoAnotacao("Resumo", "<div>texto</div>")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location",
						"http://localhost/api/categorias/" + CATEGORIA_ID + "/anotacoes/" + ANOTACAO_ID))
				.andExpect(jsonPath("$.id", matchesPattern(ID_OBJECTID)))
				.andExpect(jsonPath("$.criadaEm", matchesPattern(INSTANTE_UTC)))
				.andExpect(jsonPath("$.atualizadaEm", matchesPattern(INSTANTE_UTC)))
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
				.andExpect(jsonPath("$.criadaEm", matchesPattern(INSTANTE_UTC)))
				.andExpect(jsonPath("$.atualizadaEm", matchesPattern(INSTANTE_UTC)))
				.andExpect(jsonPath("$.titulo").value("Novo título"));
	}

	@Test
	void excluirAnotacaoDevolve204() throws Exception {
		mockMvc.perform(delete(BASE_PATH + "/{id}/anotacoes/{anotacaoId}", CATEGORIA_ID, ANOTACAO_ID))
				.andExpect(status().isNoContent());
	}

	private Categoria categoria() {
		Categoria categoria = new Categoria("Engenharia de Software", "Disciplina do 6º semestre", Cor.ROXO);
		categoria.setId(CATEGORIA_ID);
		categoria.setUsuarioId(USUARIO_ID);
		categoria.setCriadaEm(AGORA);
		categoria.setAtualizadaEm(AGORA);
		categoria.getAnotacoes().add(anotacao("672f0000000000000000c001", "Padrões de projeto"));
		categoria.getAnotacoes().add(anotacao("672f0000000000000000c002", "Arquitetura em camadas"));
		return categoria;
	}

	private Anotacao anotacao(String id, String titulo) {
		Anotacao anotacao = new Anotacao(titulo, "<div>Resumo da aula</div>");
		anotacao.setId(id);
		anotacao.setCriadaEm(AGORA);
		anotacao.setAtualizadaEm(AGORA);
		return anotacao;
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
