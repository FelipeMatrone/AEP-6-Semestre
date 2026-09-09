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

import br.com.cesumar.aep.dto.TarefaCreateRequest;
import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.service.TarefaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

	private final TarefaService service;

	public TarefaController(TarefaService service) {
		this.service = service;
	}

	@GetMapping
	public List<TarefaSummaryResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public TarefaResponse buscarPorId(@PathVariable String id) {
		return service.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaCreateRequest request) {
		TarefaResponse tarefa = service.criar(request);

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(tarefa.id())
				.toUri();

		return ResponseEntity.created(local).body(tarefa);
	}

	@PutMapping("/{id}")
	public TarefaResponse atualizar(@PathVariable String id, @Valid @RequestBody TarefaUpdateRequest request) {
		return service.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String id) {
		service.excluir(id);
	}

}
