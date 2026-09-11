package br.com.cesumar.aep.dto;

import java.time.LocalDate;

import br.com.cesumar.aep.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// sem id e sem concluida: tarefa nasce pendente, e isso é regra de negócio,
// não entrada do cliente.
public record TarefaCreateRequest(

		@NotBlank(message = "O título é obrigatório")
		String titulo,

		@NotNull(message = "O prazo é obrigatório")
		LocalDate prazo,

		@NotNull(message = "A prioridade é obrigatória")
		Prioridade prioridade,

		@NotBlank(message = "A observação é obrigatória")
		String observacao) {
}
