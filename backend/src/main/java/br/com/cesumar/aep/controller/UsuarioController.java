package br.com.cesumar.aep.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.cesumar.aep.dto.PerfilResponse;
import br.com.cesumar.aep.dto.PerfilUpdateRequest;
import br.com.cesumar.aep.dto.SenhaUpdateRequest;
import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.service.UsuarioAtual;
import br.com.cesumar.aep.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService service;

	private final UsuarioAtual usuarioAtual;

	public UsuarioController(UsuarioService service, UsuarioAtual usuarioAtual) {
		this.service = service;
		this.usuarioAtual = usuarioAtual;
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCreateRequest request) {
		UsuarioResponse usuario = service.criar(request);

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(usuario.id())
				.toUri();

		return ResponseEntity.created(local).body(usuario);
	}

	// "me" e não {id}: o dono vem da sessão, então não há id alheio para tentar.
	@PutMapping("/me")
	public PerfilResponse atualizarPerfil(@Valid @RequestBody PerfilUpdateRequest request) {
		return service.atualizarPerfil(usuarioAtual.id(), request);
	}

	@PutMapping("/me/senha")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void trocarSenha(@Valid @RequestBody SenhaUpdateRequest request) {
		service.trocarSenha(usuarioAtual.id(), request);
	}

}
