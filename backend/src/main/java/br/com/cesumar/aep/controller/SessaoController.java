package br.com.cesumar.aep.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesumar.aep.dto.SessaoCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import jakarta.validation.Valid;

// Dados fixos: a constante sai quando o Service for ligado.
@RestController
@RequestMapping("/api/sessao")
public class SessaoController {

	private static final UsuarioResponse USUARIO_FIXO = new UsuarioResponse("672f0000000000000000a001",
			"Usuário Demonstração", "demo@aep.com");

	@GetMapping
	public UsuarioResponse atual() {
		return USUARIO_FIXO;
	}

	@PostMapping
	public UsuarioResponse entrar(@Valid @RequestBody SessaoCreateRequest request) {
		return USUARIO_FIXO;
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void sair() {
	}

}
