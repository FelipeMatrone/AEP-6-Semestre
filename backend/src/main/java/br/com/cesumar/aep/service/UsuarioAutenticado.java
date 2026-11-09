package br.com.cesumar.aep.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// getUsername() devolve o id, e não o e-mail, de propósito: com isso
// Authentication.getName() já é o usuarioId que os Services comparam para
// decidir pertencimento, e nenhum controller precisa de cast para descobri-lo.
// O DaoAuthenticationProvider não compara getUsername() com o que foi digitado
// — ele carrega pelo e-mail e confere só a senha —, então a troca é inofensiva.
public record UsuarioAutenticado(
		String id,
		String nome,
		String email,
		String senhaHash) implements UserDetails {

	// o domínio não tem papéis: pertencimento é regra de Service, não de rota.
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getUsername() {
		return id;
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	// o toString() gerado pelo record carregaria o hash para qualquer log que
	// imprima o principal. Nem o hash sai daqui.
	@Override
	public String toString() {
		return "UsuarioAutenticado[id=%s, email=%s]".formatted(id, email);
	}

}
