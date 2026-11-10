package br.com.cesumar.aep.dto;

import br.com.cesumar.aep.model.Cor;

// categoriaId é nulo de propósito: a tarefa sem disciplina cai num balde sem categoria.
public record ProgressoResponse(
		String categoriaId,
		String nome,
		Cor cor,
		long total,
		long concluidas,
		int percentual) {
}
