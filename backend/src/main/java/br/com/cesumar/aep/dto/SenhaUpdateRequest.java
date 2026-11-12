package br.com.cesumar.aep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SenhaUpdateRequest(

		@NotBlank(message = "A senha atual é obrigatória")
		String senhaAtual,

		// mesmo limite do cadastro: 72 bytes é o que o BCrypt considera.
		@NotBlank(message = "A nova senha é obrigatória")
		@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
		String novaSenha) {
}
