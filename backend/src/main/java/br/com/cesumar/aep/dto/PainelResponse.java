package br.com.cesumar.aep.dto;

import java.util.List;

public record PainelResponse(
		long pendentes,
		long concluidas,
		long atrasadas,
		int metaSemanal,
		long concluidasNaSemana,
		int percentualMetaSemanal,
		List<ProgressoResponse> progresso) {
}
