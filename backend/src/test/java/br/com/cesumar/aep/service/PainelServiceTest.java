package br.com.cesumar.aep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.cesumar.aep.dto.PainelResponse;
import br.com.cesumar.aep.dto.ProgressoResponse;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.PainelRepository;
import br.com.cesumar.aep.repository.PainelRepository.CategoriaComTarefas;
import br.com.cesumar.aep.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class PainelServiceTest {

	private static final String USUARIO = "u1";

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 8);

	@Mock
	private PainelRepository repository;

	@Mock
	private TarefaRepository tarefaRepository;

	private PainelService service;

	@BeforeEach
	void montarService() {
		Clock clock = Clock.fixed(Instant.parse("2026-11-08T12:00:00Z"), ZoneOffset.UTC);
		service = new PainelService(repository, tarefaRepository, clock);
	}

	@Test
	void painelSomaPendentesConcluidasEAtrasadas() {
		when(repository.agregar(USUARIO)).thenReturn(List.of(categoria("c1", List.of(
				tarefa(true, HOJE.minusDays(5)),
				tarefa(false, HOJE.minusDays(1)),
				tarefa(false, HOJE.plusDays(1))))));
		when(tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(USUARIO))
				.thenReturn(List.of(tarefa(false, HOJE.minusDays(2))));

		PainelResponse painel = service.painel(USUARIO);

		// a concluída com prazo vencido não é atrasada
		assertThat(painel.pendentes()).isEqualTo(3);
		assertThat(painel.concluidas()).isEqualTo(1);
		assertThat(painel.atrasadas()).isEqualTo(2);
	}

	@Test
	void tarefaComPrazoHojeNaoContaComoAtrasada() {
		when(repository.agregar(USUARIO)).thenReturn(List.of(categoria("c1", List.of(
				tarefa(false, HOJE)))));
		when(tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(USUARIO)).thenReturn(List.of());

		assertThat(service.painel(USUARIO).atrasadas()).isZero();
	}

	@Test
	void tarefaSemCategoriaVaiParaOBaldeSemDisciplina() {
		when(repository.agregar(USUARIO)).thenReturn(List.of());
		when(tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(USUARIO))
				.thenReturn(List.of(tarefa(true, HOJE), tarefa(false, HOJE)));

		ProgressoResponse balde = service.painel(USUARIO).progresso().get(0);

		assertThat(balde.categoriaId()).isNull();
		assertThat(balde.nome()).isEqualTo("Sem disciplina");
		assertThat(balde.total()).isEqualTo(2);
		assertThat(balde.concluidas()).isEqualTo(1);
		assertThat(balde.percentual()).isEqualTo(50);
	}

	@Test
	void categoriaSemTarefaApareceComPercentualZero() {
		when(repository.agregar(USUARIO)).thenReturn(List.of(categoria("c1", List.of())));
		when(tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(USUARIO)).thenReturn(List.of());

		List<ProgressoResponse> progresso = service.painel(USUARIO).progresso();

		assertThat(progresso).containsExactly(new ProgressoResponse("c1", "Disciplina", Cor.ROXO, 0, 0, 0));
	}

	@Test
	void percentualVemArredondadoParaInteiro() {
		when(repository.agregar(USUARIO)).thenReturn(List.of(categoria("c1", List.of(
				tarefa(true, HOJE), tarefa(false, HOJE), tarefa(false, HOJE)))));
		when(tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(USUARIO)).thenReturn(List.of());

		// 1 de 3 = 33,33...
		assertThat(service.painel(USUARIO).progresso().get(0).percentual()).isEqualTo(33);
	}

	private CategoriaComTarefas categoria(String id, List<Tarefa> tarefas) {
		return new CategoriaComTarefas(id, "Disciplina", Cor.ROXO, tarefas);
	}

	private Tarefa tarefa(boolean concluida, LocalDate prazo) {
		Tarefa tarefa = new Tarefa();
		tarefa.setConcluida(concluida);
		tarefa.setPrazo(prazo);
		return tarefa;
	}

}
