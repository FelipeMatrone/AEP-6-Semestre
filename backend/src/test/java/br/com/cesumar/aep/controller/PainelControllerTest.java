package br.com.cesumar.aep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PainelController.class)
class PainelControllerTest {

	private static final String BASE_PATH = "/api/painel";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void painelDevolveTotaisEProgresso() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pendentes").isNumber())
				.andExpect(jsonPath("$.concluidas").isNumber())
				.andExpect(jsonPath("$.atrasadas").isNumber())
				.andExpect(jsonPath("$.progresso[0].categoriaId").isNotEmpty())
				.andExpect(jsonPath("$.progresso[0].cor").value("roxo"))
				.andExpect(jsonPath("$.progresso[0].percentual").isNumber());
	}

}
