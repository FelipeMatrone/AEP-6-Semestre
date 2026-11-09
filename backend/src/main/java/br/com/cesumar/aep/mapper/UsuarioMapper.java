package br.com.cesumar.aep.mapper;

import org.springframework.stereotype.Component;

import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.model.Usuario;

@Component
public class UsuarioMapper {

	// a senha não é traduzida: o hash é campo derivado e nasce no Service.
	public Usuario toModel(UsuarioCreateRequest request) {
		return new Usuario(request.nome(), request.email());
	}

	// senhaHash e criadoEm ficam fora da resposta de propósito.
	public UsuarioResponse toResponse(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail());
	}

}
