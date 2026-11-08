package br.com.cesumar.aep.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesumar.aep.dto.PainelResponse;
import br.com.cesumar.aep.dto.ProgressoResponse;
import br.com.cesumar.aep.model.Cor;

// Dados fixos: a constante sai quando o Service for ligado.
@RestController
@RequestMapping("/api/painel")
public class PainelController {

	private static final PainelResponse PAINEL_FIXO = new PainelResponse(7, 12, 2, List.of(
			new ProgressoResponse("672f0000000000000000b001", "Engenharia de Software", Cor.ROXO, 5, 3, 60),
			new ProgressoResponse(null, "Sem categoria", Cor.AMBAR, 2, 0, 0)));

	@GetMapping
	public PainelResponse painel() {
		return PAINEL_FIXO;
	}

}
