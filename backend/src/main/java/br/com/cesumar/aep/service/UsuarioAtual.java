package br.com.cesumar.aep.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// UsuarioAutenticado.getUsername() devolve o id, logo Authentication.getName()
// já é o usuarioId que os Services comparam — sem cast e sem ida ao banco.
// Authentication nula aqui significa cadeia de segurança furada: o
// NullPointerException em 500 é melhor do que um usuarioId nulo chegando a uma
// consulta que devolveria dados de outro dono.
@Service
public class UsuarioAtual {

	public String id() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
