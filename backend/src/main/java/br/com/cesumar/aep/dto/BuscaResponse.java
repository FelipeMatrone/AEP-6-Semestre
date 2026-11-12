package br.com.cesumar.aep.dto;

import java.util.List;

public record BuscaResponse(
		List<BuscaTarefaResponse> tarefas,
		List<BuscaCategoriaResponse> categorias,
		List<BuscaAnotacaoResponse> anotacoes) {
}
