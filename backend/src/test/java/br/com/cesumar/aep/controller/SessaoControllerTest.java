package br.com.cesumar.aep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import br.com.cesumar.aep.configuration.SecurityConfiguration;
import br.com.cesumar.aep.service.UsuarioAutenticado;
import br.com.cesumar.aep.service.UsuarioService;

// @WebMvcTest não carrega uma @Configuration qualquer: sem o @Import a cadeia
// deste projeto ficaria de fora e o slice testaria o padrão do Boot.
@WebMvcTest(SessaoController.class)
@Import(SecurityConfiguration.class)
class SessaoControllerTest {

	private static final String BASE_PATH = "/api/sessao";

	private static final String USUARIO_ID = "672f0000000000000000a001";

	private static final String NOME = "Usuário Demonstração";

	private static final String EMAIL = "demo@aep.local";

	private static final String SENHA = "aep-demo-2026";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService service;

	@Test
	void loginComCredenciaisValidasDevolve200ComUsuario() throws Exception {
		when(service.loadUserByUsername(EMAIL)).thenReturn(usuario());

		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo(SENHA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USUARIO_ID))
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	// fixação de sessão: uma sessão plantada antes do login não pode sobreviver
	// a ele. Autenticar à mão passa por fora dos filtros, então é o controller
	// que tem de rodar a SessionAuthenticationStrategy.
	@Test
	void loginTrocaOIdDaSessaoAnterior() throws Exception {
		when(service.loadUserByUsername(EMAIL)).thenReturn(usuario());

		MockHttpSession sessaoPlantada = new MockHttpSession();
		String idAntes = sessaoPlantada.getId();

		mockMvc.perform(post(BASE_PATH)
				.session(sessaoPlantada)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo(SENHA)))
				.andExpect(status().isOk());

		assertThat(sessaoPlantada.getId()).isNotEqualTo(idAntes);
	}

	@Test
	void loginComSenhaErradaDevolve401() throws Exception {
		when(service.loadUserByUsername(EMAIL)).thenReturn(usuario());

		mockMvc.perform(post(BASE_PATH)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo("senha-errada")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.path").value(BASE_PATH))
				.andExpect(jsonPath("$.message").value("Credenciais inválidas"));
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
	void getSessaoSemAutenticacaoDevolve401() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getSessaoDevolveUsuarioDoPrincipal() throws Exception {
		mockMvc.perform(get(BASE_PATH).with(user(usuario())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USUARIO_ID))
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	@Test
	@WithMockUser
	void logoutDevolve204() throws Exception {
		mockMvc.perform(delete(BASE_PATH).with(csrf()))
				.andExpect(status().isNoContent());
	}

	// encoder de verdade, e não um hash de fachada: o bean vem do @Import e é
	// ele que decide entre o 200 e o 401 dos dois primeiros testes.
	private UsuarioAutenticado usuario() {
		return new UsuarioAutenticado(USUARIO_ID, NOME, EMAIL, new BCryptPasswordEncoder().encode(SENHA));
	}

	private String corpo(String senha) {
		return """
				{ "email": "%s", "senha": "%s" }
				""".formatted(EMAIL, senha);
	}

}
