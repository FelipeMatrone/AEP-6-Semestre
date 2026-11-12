package br.com.cesumar.aep.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.cesumar.aep.dto.BuscaAnotacaoResponse;
import br.com.cesumar.aep.dto.BuscaCategoriaResponse;
import br.com.cesumar.aep.dto.BuscaResponse;
import br.com.cesumar.aep.dto.BuscaTarefaResponse;
import br.com.cesumar.aep.service.BuscaService;
import br.com.cesumar.aep.service.UsuarioAtual;

@WebMvcTest(BuscaController.class)
@Import(UsuarioAtual.class)
@WithMockUser(username = "672f0000000000000000a001")
class BuscaControllerTest {

	private static final String BASE_PATH = "/api/busca";

	private static final String USUARIO = "672f0000000000000000a001";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BuscaService service;

	@Test
	void buscarDevolveResultadosAgrupados() throws Exception {
		when(service.buscar("mongo", USUARIO)).thenReturn(new BuscaResponse(
				List.of(new BuscaTarefaResponse("t1", "Estudar MongoDB", "c1")),
				List.of(new BuscaCategoriaResponse("c1", "Banco de Dados")),
				List.of(new BuscaAnotacaoResponse("c1", "Banco de Dados", "a1", "Índices MongoDB"))));

		mockMvc.perform(get(BASE_PATH).param("termo", "mongo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tarefas[0].id").value("t1"))
				.andExpect(jsonPath("$.categorias[0].nome").value("Banco de Dados"))
				.andExpect(jsonPath("$.anotacoes[0].categoriaId").value("c1"))
				.andExpect(jsonPath("$.anotacoes[0].id").value("a1"));
	}
}
