package br.com.cesumar.aep.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cesumar.aep.model.Prioridade;

public record TarefaResponse(
		String id,
		String titulo,
		LocalDate prazo,
		Prioridade prioridade,
		boolean concluida,
		String observacao,
		Instant criadaEm,
		Instant atualizadaEm) {
}
