package br.com.cesumar.aep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnotacaoCreateRequest(

		@NotBlank(message = "O título é obrigatório")
		String titulo,

		@NotBlank(message = "O conteúdo é obrigatório")
		@Size(max = 20_000, message = "O conteúdo excede 20000 caracteres")
		String conteudo) {
}
