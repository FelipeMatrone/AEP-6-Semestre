package br.com.cesumar.aep.dto;

import java.time.LocalDate;

// a projeção da listagem. Devolver TarefaResponse aqui por engano passa
// despercebido sem o doesNotExist do teste de contrato.
public record TarefaSummaryResponse(
		String id,
		String titulo,
		LocalDate prazo,
		boolean concluida) {
}
