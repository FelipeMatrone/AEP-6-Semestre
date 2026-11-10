package br.com.cesumar.aep.dto;

import java.util.List;

import br.com.cesumar.aep.model.Cor;

public record CategoriaResponse(
		String id,
		String nome,
		String descricao,
		Cor cor,
		List<AnotacaoResponse> anotacoes) {
}
