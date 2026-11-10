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

import br.com.cesumar.aep.dto.AnotacaoCreateRequest;
import br.com.cesumar.aep.dto.AnotacaoResponse;
import br.com.cesumar.aep.dto.AnotacaoUpdateRequest;
import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaResponse;
import br.com.cesumar.aep.dto.CategoriaSummaryResponse;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.service.CategoriaService;
import br.com.cesumar.aep.service.UsuarioAtual;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService service;

	private final UsuarioAtual usuarioAtual;

	public CategoriaController(CategoriaService service, UsuarioAtual usuarioAtual) {
		this.service = service;
		this.usuarioAtual = usuarioAtual;
	}

	@GetMapping
	public List<CategoriaSummaryResponse> listar() {
		return service.listar(usuarioAtual.id());
	}

	@GetMapping("/{id}")
	public CategoriaResponse buscarPorId(@PathVariable String id) {
		return service.buscarPorId(id, usuarioAtual.id());
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaCreateRequest request) {
		CategoriaResponse categoria = service.criar(request, usuarioAtual.id());

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(categoria.id())
				.toUri();

		return ResponseEntity.created(local).body(categoria);
	}

	@PutMapping("/{id}")
	public CategoriaResponse atualizar(@PathVariable String id, @Valid @RequestBody CategoriaUpdateRequest request) {
		return service.atualizar(id, request, usuarioAtual.id());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String id) {
		service.excluir(id, usuarioAtual.id());
	}

	@PostMapping("/{id}/anotacoes")
	public ResponseEntity<AnotacaoResponse> criarAnotacao(@PathVariable String id,
			@Valid @RequestBody AnotacaoCreateRequest request) {
		AnotacaoResponse anotacao = service.criarAnotacao(id, request, usuarioAtual.id());

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{anotacaoId}")
				.buildAndExpand(anotacao.id())
				.toUri();

		return ResponseEntity.created(local).body(anotacao);
	}

	@PutMapping("/{id}/anotacoes/{anotacaoId}")
	public AnotacaoResponse atualizarAnotacao(@PathVariable String id, @PathVariable String anotacaoId,
			@Valid @RequestBody AnotacaoUpdateRequest request) {
		return service.atualizarAnotacao(id, anotacaoId, request, usuarioAtual.id());
	}

	@DeleteMapping("/{id}/anotacoes/{anotacaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluirAnotacao(@PathVariable String id, @PathVariable String anotacaoId) {
		service.excluirAnotacao(id, anotacaoId, usuarioAtual.id());
	}

}
