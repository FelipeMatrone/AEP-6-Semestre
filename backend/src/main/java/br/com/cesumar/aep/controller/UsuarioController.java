package br.com.cesumar.aep.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import jakarta.validation.Valid;

// Dados fixos: a constante sai quando o Service for ligado.
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private static final String USUARIO_FIXO = "672f0000000000000000a001";

	@PostMapping
	public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCreateRequest request) {
		UsuarioResponse usuario = new UsuarioResponse(USUARIO_FIXO, request.nome(), request.email());

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(usuario.id())
				.toUri();

		return ResponseEntity.created(local).body(usuario);
	}

}
