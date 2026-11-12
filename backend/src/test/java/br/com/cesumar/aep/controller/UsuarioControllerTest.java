package br.com.cesumar.aep.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.cesumar.aep.dto.PerfilResponse;
import br.com.cesumar.aep.dto.PerfilUpdateRequest;
import br.com.cesumar.aep.dto.SenhaUpdateRequest;
import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.exception.EmailJaCadastradoException;
import br.com.cesumar.aep.exception.SenhaAtualIncorretaException;
import br.com.cesumar.aep.service.UsuarioAtual;
import br.com.cesumar.aep.service.UsuarioService;

// o @WithMockUser sem nome vira o usuarioId "user" pelo UsuarioAtual de verdade.
@WebMvcTest(UsuarioController.class)
@Import(UsuarioAtual.class)
@WithMockUser
class UsuarioControllerTest {

	private static final String BASE_PATH = "/api/usuarios";

	private static final String USUARIO_ID = "672f0000000000000000a001";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService service;

	@Test
	void cadastrarDevolve201ComLocation() throws Exception {
		when(service.criar(any(UsuarioCreateRequest.class)))
				.thenReturn(new UsuarioResponse(USUARIO_ID, "Ana", "ana@aep.com"));

		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("ana@aep.com")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/usuarios/" + USUARIO_ID))
				.andExpect(jsonPath("$.id").value(USUARIO_ID))
				.andExpect(jsonPath("$.nome").value("Ana"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	@Test
	void cadastrarComEmailInvalidoDevolve400() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("nao-e-email")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.email").value("O e-mail é inválido"));
	}

	@Test
	void cadastrarComEmailJaCadastradoDevolve409() throws Exception {
		when(service.criar(any(UsuarioCreateRequest.class)))
				.thenThrow(new EmailJaCadastradoException("ana@aep.com"));

		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("ana@aep.com")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.path").value(BASE_PATH))
				.andExpect(jsonPath("$.message").value("E-mail já cadastrado: ana@aep.com"))
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	// a corrida entre duas requisições simultâneas chega aqui como erro do
	// índice único, e tem de sair como o mesmo 409, não como 500.
	@Test
	void cadastrarComChaveDuplicadaNoBancoDevolve409() throws Exception {
		when(service.criar(any(UsuarioCreateRequest.class)))
				.thenThrow(new DuplicateKeyException("E11000 duplicate key error"));

		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("ana@aep.com")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
	}

	@Test
	void atualizarPerfilUsaOUsuarioDaSessao() throws Exception {
		when(service.atualizarPerfil(eq("user"), any(PerfilUpdateRequest.class)))
				.thenReturn(new PerfilResponse("user", "Ana Maria", "ana@aep.com", 10, null));

		mockMvc.perform(put(BASE_PATH + "/me")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "nome": "Ana Maria", "metaSemanal": 10 }
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Ana Maria"))
				.andExpect(jsonPath("$.metaSemanal").value(10));
	}

	@Test
	void atualizarPerfilComMetaForaDaFaixaDevolve400() throws Exception {
		mockMvc.perform(put(BASE_PATH + "/me")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "nome": "Ana", "metaSemanal": 0 }
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.metaSemanal").value("A meta semanal deve ser de 1 a 100 tarefas"));
	}

	@Test
	void trocarSenhaDevolve204() throws Exception {
		mockMvc.perform(put(BASE_PATH + "/me/senha")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoSenha()))
				.andExpect(status().isNoContent());
	}

	@Test
	void trocarSenhaComSenhaAtualErradaMarcaOCampo() throws Exception {
		doThrow(new SenhaAtualIncorretaException()).when(service)
				.trocarSenha(eq("user"), any(SenhaUpdateRequest.class));

		mockMvc.perform(put(BASE_PATH + "/me/senha")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoSenha()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.senhaAtual").value("A senha atual não confere"));
	}

	private String corpoSenha() {
		return """
				{ "senhaAtual": "senha-segura-1", "novaSenha": "nova-senha-2026" }
				""";
	}

	private String corpo(String email) {
		return """
				{ "nome": "Ana", "email": "%s", "senha": "senha-segura-1" }
				""".formatted(email);
	}

}
