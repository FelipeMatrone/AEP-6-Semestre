package br.com.cesumar.aep.mapper;

import org.springframework.stereotype.Component;

import br.com.cesumar.aep.dto.TarefaCreateRequest;
import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.model.Tarefa;

@Component
public class TarefaMapper {

	// traduz apenas o que veio do cliente. concluida, criadaEm e atualizadaEm
	// ficam como estão — quem os define é o Service.
	public Tarefa toModel(TarefaCreateRequest request) {
		return new Tarefa(request.titulo(), request.prazo(), request.prioridade(), request.observacao());
	}

	// atualiza a instância persistida no lugar, o que preserva o id.
	public void updateModel(Tarefa tarefa, TarefaUpdateRequest request) {
		tarefa.setTitulo(request.titulo());
		tarefa.setPrazo(request.prazo());
		tarefa.setPrioridade(request.prioridade());
		tarefa.setObservacao(request.observacao());
		tarefa.setConcluida(request.concluida());
	}

	public TarefaResponse toResponse(Tarefa tarefa) {
		return new TarefaResponse(
				tarefa.getId(),
				tarefa.getTitulo(),
				tarefa.getPrazo(),
				tarefa.getPrioridade(),
				tarefa.isConcluida(),
				tarefa.getObservacao(),
				tarefa.getCriadaEm(),
				tarefa.getAtualizadaEm());
	}

	public TarefaSummaryResponse toSummaryResponse(Tarefa tarefa) {
		return new TarefaSummaryResponse(
				tarefa.getId(),
				tarefa.getTitulo(),
				tarefa.getPrazo(),
				tarefa.getPrioridade(),
				tarefa.getObservacao(),
				tarefa.isConcluida());
	}

}
