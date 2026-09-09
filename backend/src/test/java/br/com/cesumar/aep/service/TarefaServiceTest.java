package br.com.cesumar.aep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.cesumar.aep.dto.TarefaCreateRequest;
import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.exception.TarefaNotFoundException;
import br.com.cesumar.aep.mapper.TarefaMapper;
import br.com.cesumar.aep.model.Prioridade;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-09T12:00:00Z");

	private static final Instant ONTEM = Instant.parse("2026-09-08T12:00:00Z");

	private static final LocalDate PRAZO = LocalDate.of(2026, 9, 12);

	@Mock
	private TarefaRepository repository;

	private TarefaService service;

	@BeforeEach
	void prepararService() {
		// o Mapper entra de verdade, não mockado: ele não tem dependência nenhuma e
		// mocká-lo faria as asserções sobre o DTO conferirem o mock, não a tradução.
		// O relógio fixo é o que permite exigir valor exato em criadaEm e
		// atualizadaEm, em vez de "não é nulo".
		service = new TarefaService(repository, new TarefaMapper(), Clock.fixed(AGORA, ZoneOffset.UTC));
	}

	@Test
	void deveListarTarefasResumidas() {
		when(repository.findAll()).thenReturn(List.of(tarefa("1", "Entregar a modelagem", false)));

		List<TarefaSummaryResponse> resumos = service.listar();

		assertThat(resumos).containsExactly(new TarefaSummaryResponse("1", "Entregar a modelagem", PRAZO, false));
	}

	@Test
	void deveBuscarTarefaExistente() {
		when(repository.findById("1")).thenReturn(Optional.of(tarefa("1", "Entregar a modelagem", false)));

		TarefaResponse resposta = service.buscarPorId("1");

		assertThat(resposta.titulo()).isEqualTo("Entregar a modelagem");
		assertThat(resposta.prioridade()).isEqualTo(Prioridade.MEDIA);
		assertThat(resposta.dono()).isEqualTo("samuel");
	}

	@Test
	void deveLancarExcecaoAoBuscarTarefaInexistente() {
		when(repository.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscarPorId("999"))
				.isInstanceOf(TarefaNotFoundException.class)
				.hasMessageContaining("999");
	}

	@Test
	void deveCriarTarefaPendenteCarimbandoAsDatas() {
		when(repository.save(any(Tarefa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		TarefaResponse resposta = service.criar(
				new TarefaCreateRequest("Definir o ODS", PRAZO, Prioridade.MEDIA, "samuel"));

		assertThat(resposta.concluida()).isFalse();
		assertThat(resposta.criadaEm()).isEqualTo(AGORA);
		assertThat(resposta.atualizadaEm()).isEqualTo(AGORA);
	}

	@Test
	void deveAtualizarPreservandoOIdEACriacao() {
		Tarefa persistida = tarefa("1", "Título antigo", false);
		persistida.setCriadaEm(ONTEM);
		persistida.setAtualizadaEm(ONTEM);
		when(repository.findById("1")).thenReturn(Optional.of(persistida));
		when(repository.save(any(Tarefa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		TarefaResponse resposta = service.atualizar("1",
				new TarefaUpdateRequest("Título novo", PRAZO, Prioridade.ALTA, true));

		assertThat(resposta.id()).isEqualTo("1");
		assertThat(resposta.titulo()).isEqualTo("Título novo");
		assertThat(resposta.dono()).isEqualTo("samuel");
		assertThat(resposta.criadaEm()).isEqualTo(ONTEM);
		assertThat(resposta.atualizadaEm()).isEqualTo(AGORA);
	}

	@Test
	void deveExcluirTarefaExistente() {
		Tarefa persistida = tarefa("1", "Entregar a modelagem", false);
		when(repository.findById("1")).thenReturn(Optional.of(persistida));

		service.excluir("1");

		verify(repository).delete(persistida);
	}

	@Test
	void deveLancarExcecaoAoExcluirTarefaInexistente() {
		when(repository.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.excluir("999")).isInstanceOf(TarefaNotFoundException.class);

		verify(repository, never()).delete(any(Tarefa.class));
	}

	private Tarefa tarefa(String id, String titulo, boolean concluida) {
		Tarefa tarefa = new Tarefa(titulo, PRAZO, Prioridade.MEDIA, "samuel");
		tarefa.setId(id);
		tarefa.setConcluida(concluida);
		tarefa.setCriadaEm(AGORA);
		tarefa.setAtualizadaEm(AGORA);
		return tarefa;
	}

}
