package br.com.cesumar.aep.dto;

import java.time.Instant;

public record AnotacaoResponse(
		String id,
		String titulo,
		String conteudo,
		Instant criadaEm,
		Instant atualizadaEm) {
}
