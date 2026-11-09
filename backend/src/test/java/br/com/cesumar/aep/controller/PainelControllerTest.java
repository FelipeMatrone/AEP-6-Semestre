package br.com.cesumar.aep.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PainelController.class)
@WithMockUser
class PainelControllerTest {

	private static final String BASE_PATH = "/api/painel";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void painelDevolveTotaisEProgresso() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pendentes").value(7))
				.andExpect(jsonPath("$.concluidas").value(12))
				.andExpect(jsonPath("$.atrasadas").value(2))
				.andExpect(jsonPath("$.progresso[0].categoriaId", matchesPattern("[0-9a-f]{24}")))
				.andExpect(jsonPath("$.progresso[0].cor").value("roxo"))
				.andExpect(jsonPath("$.progresso[0].total").value(5))
				.andExpect(jsonPath("$.progresso[0].concluidas").value(3))
				// inteiro e já arredondado pelo backend, não 60.0
				.andExpect(jsonPath("$.progresso[0].percentual").value(60));
	}

	@Test
	void painelDevolveBaldeSemDisciplinaComCategoriaIdNulo() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.progresso[1].categoriaId").value(nullValue()))
				.andExpect(jsonPath("$.progresso[1].nome").value("Sem disciplina"))
				.andExpect(jsonPath("$.progresso[1].percentual").value(0));
	}

}
