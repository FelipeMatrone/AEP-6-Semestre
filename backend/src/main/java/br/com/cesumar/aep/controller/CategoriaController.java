package br.com.cesumar.aep.controller;

import java.net.URI;
import java.time.Instant;
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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	// usuário semeado em banco/init.js. Sai quando o Security fornecer o usuário
	// autenticado.
	private static final String USUARIO_FIXO = "672f0000000000000000a001";

	// Dados fixos das rotas de anotação: saem quando elas forem ligadas ao Service.
	private static final String ANOTACAO_ID_FIXO = "672f0000000000000000c001";

	private static final Instant CARIMBO_FIXO = Instant.parse("2026-11-08T14:32:00Z");

	private final CategoriaService service;

	public CategoriaController(CategoriaService service) {
		this.service = service;
	}

	@GetMapping
	public List<CategoriaSummaryResponse> listar() {
		return service.listar(USUARIO_FIXO);
	}

	@GetMapping("/{id}")
	public CategoriaResponse buscarPorId(@PathVariable String id) {
		return service.buscarPorId(id, USUARIO_FIXO);
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaCreateRequest request) {
		CategoriaResponse categoria = service.criar(request, USUARIO_FIXO);

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(categoria.id())
				.toUri();

		return ResponseEntity.created(local).body(categoria);
	}

	@PutMapping("/{id}")
	public CategoriaResponse atualizar(@PathVariable String id, @Valid @RequestBody CategoriaUpdateRequest request) {
		return service.atualizar(id, request, USUARIO_FIXO);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String id) {
		service.excluir(id, USUARIO_FIXO);
	}

	@PostMapping("/{id}/anotacoes")
	public ResponseEntity<AnotacaoResponse> criarAnotacao(@PathVariable String id,
			@Valid @RequestBody AnotacaoCreateRequest request) {
		AnotacaoResponse anotacao = new AnotacaoResponse(ANOTACAO_ID_FIXO, request.titulo(), request.conteudo(),
				CARIMBO_FIXO, CARIMBO_FIXO);

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{anotacaoId}")
				.buildAndExpand(anotacao.id())
				.toUri();

		return ResponseEntity.created(local).body(anotacao);
	}

	@PutMapping("/{id}/anotacoes/{anotacaoId}")
	public AnotacaoResponse atualizarAnotacao(@PathVariable String id, @PathVariable String anotacaoId,
			@Valid @RequestBody AnotacaoUpdateRequest request) {
		return new AnotacaoResponse(anotacaoId, request.titulo(), request.conteudo(), CARIMBO_FIXO, CARIMBO_FIXO);
	}

	@DeleteMapping("/{id}/anotacoes/{anotacaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluirAnotacao(@PathVariable String id, @PathVariable String anotacaoId) {
	}

}
