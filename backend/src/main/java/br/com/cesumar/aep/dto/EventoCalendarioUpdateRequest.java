package br.com.cesumar.aep.dto;

import java.time.LocalDate;

import br.com.cesumar.aep.model.Cor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventoCalendarioUpdateRequest(
		@NotBlank(message = "O título é obrigatório") String titulo,
		String descricao,
		@NotNull(message = "A data é obrigatória") LocalDate data,
		String horario,
		@NotNull(message = "A cor é obrigatória") Cor cor,
		String tarefaId) {
}
