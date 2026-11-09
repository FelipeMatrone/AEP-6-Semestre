package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.PainelResponse;
import br.com.cesumar.aep.dto.ProgressoResponse;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.PainelRepository;
import br.com.cesumar.aep.repository.PainelRepository.CategoriaComTarefas;
import br.com.cesumar.aep.repository.TarefaRepository;

@Service
public class PainelService {

	private static final String SEM_DISCIPLINA = "Sem disciplina";

	private final PainelRepository repository;

	private final TarefaRepository tarefaRepository;

	private final Clock clock;

	public PainelService(PainelRepository repository, TarefaRepository tarefaRepository, Clock clock) {
		this.repository = repository;
		this.tarefaRepository = tarefaRepository;
		this.clock = clock;
	}

	public PainelResponse painel(String usuarioId) {
		LocalDate hoje = LocalDate.now(clock);

		List<ProgressoResponse> progresso = new ArrayList<>();
		List<Tarefa> todas = new ArrayList<>();

		for (CategoriaComTarefas categoria : repository.agregar(usuarioId)) {
			progresso.add(progresso(categoria.id(), categoria.nome(), categoria.cor(), categoria.tarefas()));
			todas.addAll(categoria.tarefas());
		}

		List<Tarefa> semDisciplina = tarefaRepository.findByUsuarioIdAndCategoriaIdIsNull(usuarioId);
		if (!semDisciplina.isEmpty()) {
			progresso.add(progresso(null, SEM_DISCIPLINA, Cor.AMBAR, semDisciplina));
			todas.addAll(semDisciplina);
		}

		long concluidas = todas.stream().filter(Tarefa::isConcluida).count();
		long atrasadas = todas.stream().filter(tarefa -> atrasada(tarefa, hoje)).count();

		return new PainelResponse(todas.size() - concluidas, concluidas, atrasadas, progresso);
	}

	private ProgressoResponse progresso(String id, String nome, Cor cor, List<Tarefa> tarefas) {
		long total = tarefas.size();
		long concluidas = tarefas.stream().filter(Tarefa::isConcluida).count();
		int percentual = total == 0 ? 0 : (int) Math.round(100.0 * concluidas / total);

		return new ProgressoResponse(id, nome, cor, total, concluidas, percentual);
	}

	// fronteira estrita: prazo igual a hoje ainda não venceu.
	private boolean atrasada(Tarefa tarefa, LocalDate hoje) {
		return !tarefa.isConcluida() && tarefa.getPrazo() != null && tarefa.getPrazo().isBefore(hoje);
	}

}
