package br.com.cesumar.aep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(

		@NotBlank(message = "O nome é obrigatório")
		String nome,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail é inválido")
		String email,

		// 72 é o limite de bytes que o BCrypt considera; senha maior seria truncada em silêncio.
		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
		String senha) {
}
