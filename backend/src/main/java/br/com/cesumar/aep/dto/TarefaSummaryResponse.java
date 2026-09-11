package br.com.cesumar.aep.dto;

import java.time.LocalDate;

import br.com.cesumar.aep.model.Prioridade;

// a projeção da listagem. Devolver TarefaResponse aqui por engano passa
// despercebido sem o doesNotExist do teste de contrato.
public record TarefaSummaryResponse(
		String id,
		String titulo,
		LocalDate prazo,
		Prioridade prioridade,
		boolean concluida) {
}
