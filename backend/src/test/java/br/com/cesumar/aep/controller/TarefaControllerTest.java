package br.com.cesumar.aep.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.exception.TarefaNotFoundException;
import br.com.cesumar.aep.model.Prioridade;
import br.com.cesumar.aep.service.TarefaService;

@WebMvcTest(TarefaController.class)
class TarefaControllerTest {

	private static final String BASE_PATH = "/api/tarefas";

	private static final LocalDate PRAZO = LocalDate.of(2026, 9, 12);

	private static final Instant CARIMBO = Instant.parse("2026-09-09T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TarefaService service;

	@Test
	void deveListarSomenteOsCamposDoResumo() throws Exception {
		when(service.listar()).thenReturn(
				List.of(new TarefaSummaryResponse("1", "Entregar a modelagem", PRAZO, Prioridade.MEDIA,
						"Observação de teste", false)));

		mockMvc.perform(get(BASE_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("1"))
				.andExpect(jsonPath("$[0].titulo").value("Entregar a modelagem"))
				.andExpect(jsonPath("$[0].prazo").value("2026-09-12"))
				.andExpect(jsonPath("$[0].prioridade").value("media"))
				.andExpect(jsonPath("$[0].observacao").value("Observação de teste"))
				.andExpect(jsonPath("$[0].concluida").value(false))
				// sem estes, um TarefaResponse devolvido por engano na listagem
				// passaria verde e a projeção não estaria testada
				.andExpect(jsonPath("$[0].criadaEm").doesNotExist());
	}

	@Test
	void deveDevolverARepresentacaoCompletaNaBusca() throws Exception {
		when(service.buscarPorId("1")).thenReturn(resposta());

		mockMvc.perform(get(BASE_PATH + "/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.prioridade").value("media"))
				.andExpect(jsonPath("$.observacao").value("Observação de teste"))
				.andExpect(jsonPath("$.criadaEm").value("2026-09-09T12:00:00Z"));
	}

	@Test
	void deveCriarComLocationEOCorpoCompleto() throws Exception {
		when(service.criar(any())).thenReturn(resposta());

		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "Entregar a modelagem",
						  "prazo": "2026-09-12",
						  "prioridade": "media",
						  "observacao": "Observação de teste"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/tarefas/1"))
				.andExpect(jsonPath("$.id").value("1"))
				.andExpect(jsonPath("$.concluida").value(false));
	}

	@Test
	void deveRejeitarCorpoInvalidoComFieldErrors() throws Exception {
		mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "  ",
						  "prioridade": "media",
						  "observacao": "Observação de teste"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value(BASE_PATH))
				.andExpect(jsonPath("$.fieldErrors.titulo").value("O título é obrigatório"))
				.andExpect(jsonPath("$.fieldErrors.prazo").value("O prazo é obrigatório"));
	}

	@Test
	void deveRejeitarPrioridadeDesconhecida() throws Exception {
		mockMvc.perform(put(BASE_PATH + "/{id}", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "Entregar a modelagem",
						  "prazo": "2026-09-12",
						  "prioridade": "urgentissima",
						  "observacao": "Observação de teste",
						  "concluida": false
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void deveAtualizarDevolvendoARepresentacaoCompleta() throws Exception {
		when(service.atualizar(eq("1"), any(TarefaUpdateRequest.class))).thenReturn(resposta());

		mockMvc.perform(put(BASE_PATH + "/{id}", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "titulo": "Entregar a modelagem",
						  "prazo": "2026-09-12",
						  "prioridade": "media",
						  "observacao": "Observação atualizada",
						  "concluida": false
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.atualizadaEm").value("2026-09-09T12:00:00Z"));
	}

	@Test
	void deveRetornarNotFoundParaTarefaInexistente() throws Exception {
		when(service.buscarPorId("999")).thenThrow(new TarefaNotFoundException("999"));

		mockMvc.perform(get(BASE_PATH + "/{id}", "999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Tarefa não encontrada: 999"))
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void deveExcluirSemCorpo() throws Exception {
		mockMvc.perform(delete(BASE_PATH + "/{id}", "1"))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
	}

	@Test
	void deveRetornarNotFoundAoExcluirTarefaInexistente() throws Exception {
		doThrow(new TarefaNotFoundException("999")).when(service).excluir("999");

		mockMvc.perform(delete(BASE_PATH + "/{id}", "999"))
				.andExpect(status().isNotFound());
	}

	private TarefaResponse resposta() {
		return new TarefaResponse("1", "Entregar a modelagem", PRAZO, Prioridade.MEDIA, false, "Observação de teste", CARIMBO,
				CARIMBO);
	}

}
