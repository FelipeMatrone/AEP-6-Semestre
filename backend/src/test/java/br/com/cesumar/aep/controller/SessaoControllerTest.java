package br.com.cesumar.aep.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SessaoController.class)
@WithMockUser
class SessaoControllerTest {

	private static final String BASE_PATH = "/api/sessao";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getDevolveUsuarioDaSessao() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("672f0000000000000000a001"))
				.andExpect(jsonPath("$.nome").isNotEmpty())
				.andExpect(jsonPath("$.email").isNotEmpty())
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void loginDevolveUsuario() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "demo@aep.com", "senha": "qualquer" }
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("672f0000000000000000a001"))
				.andExpect(jsonPath("$.email").isNotEmpty());
	}

	@Test
	void loginComEmailInvalidoDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "nao-e-email", "senha": "qualquer" }
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.email").value("O e-mail é inválido"));
	}

	@Test
	void logoutDevolve204() throws Exception {
		mockMvc.perform(delete(BASE_PATH).with(csrf()))
				.andExpect(status().isNoContent());
	}

}
