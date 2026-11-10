package br.com.cesumar.aep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesumar.aep.dto.PainelResponse;
import br.com.cesumar.aep.service.PainelService;
import br.com.cesumar.aep.service.UsuarioAtual;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

	private final PainelService service;

	private final UsuarioAtual usuarioAtual;

	public PainelController(PainelService service, UsuarioAtual usuarioAtual) {
		this.service = service;
		this.usuarioAtual = usuarioAtual;
	}

	@GetMapping
	public PainelResponse painel() {
		return service.painel(usuarioAtual.id());
	}

}
