package br.com.cesumar.aep.dto;

import java.time.LocalDate;

import br.com.cesumar.aep.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// sem id: ele vem do path. concluida é Boolean, e não boolean, para que a
// ausência do campo seja rejeitada em vez de virar false calada.
public record TarefaUpdateRequest(

		@NotBlank(message = "O título é obrigatório")
		String titulo,

		@NotNull(message = "O prazo é obrigatório")
		LocalDate prazo,

		@NotNull(message = "A prioridade é obrigatória")
		Prioridade prioridade,

		String observacao,

		@NotNull(message = "A situação de conclusão é obrigatória")
		Boolean concluida) {
}
