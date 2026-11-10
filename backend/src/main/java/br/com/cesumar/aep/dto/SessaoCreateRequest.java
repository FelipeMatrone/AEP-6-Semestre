package br.com.cesumar.aep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SessaoCreateRequest(

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail é inválido")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		String senha) {
}
