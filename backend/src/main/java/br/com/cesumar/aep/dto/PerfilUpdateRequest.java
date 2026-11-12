package br.com.cesumar.aep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PerfilUpdateRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
		String nome,

		@NotNull(message = "A meta semanal é obrigatória")
		@Min(value = 1, message = "A meta semanal deve ser de 1 a 100 tarefas")
		@Max(value = 100, message = "A meta semanal deve ser de 1 a 100 tarefas")
		Integer metaSemanal) {
}
