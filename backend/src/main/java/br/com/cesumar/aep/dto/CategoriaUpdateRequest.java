package br.com.cesumar.aep.dto;

import br.com.cesumar.aep.model.Cor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaUpdateRequest(

		@NotBlank(message = "O nome é obrigatório")
		String nome,

		String descricao,

		@NotNull(message = "A cor é obrigatória")
		Cor cor) {
}
