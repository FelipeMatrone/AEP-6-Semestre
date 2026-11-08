package br.com.cesumar.aep.dto;

import br.com.cesumar.aep.model.Cor;

public record CategoriaSummaryResponse(
		String id,
		String nome,
		String descricao,
		Cor cor,
		int totalAnotacoes) {
}
