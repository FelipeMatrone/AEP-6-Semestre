package br.com.cesumar.aep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesumar.aep.dto.BuscaResponse;
import br.com.cesumar.aep.service.BuscaService;
import br.com.cesumar.aep.service.UsuarioAtual;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

	private final BuscaService service;

	private final UsuarioAtual usuarioAtual;

	public BuscaController(BuscaService service, UsuarioAtual usuarioAtual) {
		this.service = service;
		this.usuarioAtual = usuarioAtual;
	}

	@GetMapping
	public BuscaResponse buscar(@RequestParam String termo) {
		return service.buscar(termo, usuarioAtual.id());
	}
}
