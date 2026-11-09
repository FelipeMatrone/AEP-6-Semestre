package br.com.cesumar.aep.controller;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.cesumar.aep.configuration.SecurityConfiguration;
import br.com.cesumar.aep.service.TarefaService;
import br.com.cesumar.aep.service.UsuarioService;

// o contrato de segurança de uma rota protegida qualquer vive aqui, separado do
// TarefaControllerTest: lá todo teste passa autenticado, e um slice só prova o
// 401 e o 403 se alguma requisição chegar sem credencial e sem token.
@WebMvcTest(TarefaController.class)
@Import(SecurityConfiguration.class)
class TarefaControllerSegurancaTest {

	private static final String BASE_PATH = "/api/tarefas";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TarefaService service;

	// o AuthenticationManager do @Import se monta a partir do UserDetailsService
	// do contexto; sem este mock o slice cairia no usuário em memória do Boot.
	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void listarSemAutenticacaoDevolve401() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isUnauthorized());
	}

	// o frontend precisa do cookie já na primeira requisição, que é justamente
	// a que volta 401 — por isso a afirmação é sobre a resposta negada.
	// Vive neste slice e não no do login porque o pós-processador csrf() do
	// spring-security-test troca o repositório de token do CsrfFilter do
	// contexto inteiro: qualquer teste que o use deixa os seguintes sem cookie.
	@Test
	void respostaDe401PlantaOCookieDoTokenCsrf() throws Exception {
		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(cookie().value("XSRF-TOKEN", not(emptyString())));
	}

	// autenticado de propósito: sem isso o 403 poderia estar vindo da falta de
	// sessão, e o teste não provaria nada sobre o CSRF.
	@Test
	@WithMockUser
	void postSemTokenCsrfDevolve403() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "titulo": "Entregar a modelagem", "prioridade": "media" }
						"""))
				.andExpect(status().isForbidden());
	}

}
