package br.com.cesumar.aep.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cesumar.aep.model.Cor;

public record EventoCalendarioResponse(String id, String titulo, String descricao, LocalDate data, String horario,
		Cor cor, String tarefaId, Instant criadaEm, Instant atualizadaEm) {
}
