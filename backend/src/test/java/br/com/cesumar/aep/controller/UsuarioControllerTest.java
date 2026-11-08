package br.com.cesumar.aep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

	private static final String BASE_PATH = "/api/usuarios";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cadastrarDevolve201ComLocation() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("ana@aep.com")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/usuarios/672f0000000000000000a001"))
				.andExpect(jsonPath("$.id").value("672f0000000000000000a001"))
				.andExpect(jsonPath("$.nome").value("Ana"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void cadastrarComEmailInvalidoDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("nao-e-email")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.email").value("O e-mail é inválido"));
	}

	private String corpo(String email) {
		return """
				{ "nome": "Ana", "email": "%s", "senha": "senha-segura-1" }
				""".formatted(email);
	}

}
