package br.com.cesumar.aep.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.cesumar.aep.dto.EventoCalendarioCreateRequest;
import br.com.cesumar.aep.dto.EventoCalendarioResponse;
import br.com.cesumar.aep.dto.EventoCalendarioUpdateRequest;
import br.com.cesumar.aep.service.EventoCalendarioService;
import br.com.cesumar.aep.service.UsuarioAtual;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos-calendario")
public class EventoCalendarioController {

	private final EventoCalendarioService service;

	private final UsuarioAtual usuarioAtual;

	public EventoCalendarioController(EventoCalendarioService service, UsuarioAtual usuarioAtual) {
		this.service = service;
		this.usuarioAtual = usuarioAtual;
	}

	@GetMapping
	public List<EventoCalendarioResponse> listar() {
		return service.listar(usuarioAtual.id());
	}

	@PostMapping
	public ResponseEntity<EventoCalendarioResponse> criar(@Valid @RequestBody EventoCalendarioCreateRequest request) {
		EventoCalendarioResponse evento = service.criar(request, usuarioAtual.id());
		URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(evento.id()).toUri();
		return ResponseEntity.created(local).body(evento);
	}

	@PutMapping("/{id}")
	public EventoCalendarioResponse atualizar(@PathVariable String id,
			@Valid @RequestBody EventoCalendarioUpdateRequest request) {
		return service.atualizar(id, request, usuarioAtual.id());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String id) {
		service.excluir(id, usuarioAtual.id());
	}
}
