package br.com.cesumar.aep.dto;

import java.time.Instant;

public record PerfilResponse(
		String id,
		String nome,
		String email,
		int metaSemanal,
		Instant criadoEm) {
}
