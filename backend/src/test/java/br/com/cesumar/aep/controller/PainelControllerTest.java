package br.com.cesumar.aep.controller;

import static org.hamcrest.Matchers.nullValue;
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

import br.com.cesumar.aep.dto.PainelResponse;
import br.com.cesumar.aep.dto.ProgressoResponse;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.service.PainelService;
import br.com.cesumar.aep.service.UsuarioAtual;

@WebMvcTest(PainelController.class)
@Import(UsuarioAtual.class)
@WithMockUser(username = "672f0000000000000000a001")
class PainelControllerTest {

	private static final String BASE_PATH = "/api/painel";

	private static final String USUARIO = "672f0000000000000000a001";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PainelService service;

	@Test
	void painelDevolveTotaisEProgresso() throws Exception {
		when(service.painel(USUARIO)).thenReturn(new PainelResponse(7, 12, 2, 5, 3, 60, List.of(
				new ProgressoResponse("672f0000000000000000b001", "Engenharia de Software", Cor.ROXO, 5, 3, 60))));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pendentes").value(7))
				.andExpect(jsonPath("$.concluidas").value(12))
				.andExpect(jsonPath("$.atrasadas").value(2))
				.andExpect(jsonPath("$.metaSemanal").value(5))
				.andExpect(jsonPath("$.concluidasNaSemana").value(3))
				.andExpect(jsonPath("$.percentualMetaSemanal").value(60))
				.andExpect(jsonPath("$.progresso[0].categoriaId").value("672f0000000000000000b001"))
				.andExpect(jsonPath("$.progresso[0].cor").value("roxo"))
				.andExpect(jsonPath("$.progresso[0].total").value(5))
				.andExpect(jsonPath("$.progresso[0].concluidas").value(3))
				// inteiro e já arredondado pelo backend, não 60.0
				.andExpect(jsonPath("$.progresso[0].percentual").value(60));
	}

	@Test
	void painelDevolveBaldeSemDisciplinaComCategoriaIdNulo() throws Exception {
		when(service.painel(USUARIO)).thenReturn(new PainelResponse(2, 0, 0, 5, 0, 0, List.of(
				new ProgressoResponse(null, "Sem disciplina", Cor.AMBAR, 2, 0, 0))));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.progresso[0].categoriaId").value(nullValue()))
				.andExpect(jsonPath("$.progresso[0].nome").value("Sem disciplina"))
				.andExpect(jsonPath("$.progresso[0].percentual").value(0));
	}

}
