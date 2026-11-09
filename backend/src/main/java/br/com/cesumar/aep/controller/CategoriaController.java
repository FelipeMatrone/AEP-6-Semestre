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
import br.com.cesumar.aep.model.Cor;
import jakarta.validation.Valid;

// Dados fixos: as constantes saem quando o Service for ligado.
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private static final String CATEGORIA_ID_FIXO = "672f0000000000000000b001";

	private static final String ANOTACAO_ID_FIXO = "672f0000000000000000c001";

	private static final Instant CARIMBO_FIXO = Instant.parse("2026-11-08T14:32:00Z");

	private static final String ANOTACAO_ID_FIXO_2 = "672f0000000000000000c002";

	private static final AnotacaoResponse ANOTACAO_FIXA = new AnotacaoResponse(ANOTACAO_ID_FIXO,
			"Padrões de projeto", "<div>Resumo da aula</div>", CARIMBO_FIXO, CARIMBO_FIXO);

	private static final AnotacaoResponse ANOTACAO_FIXA_2 = new AnotacaoResponse(ANOTACAO_ID_FIXO_2,
			"Arquitetura em camadas", "<div>Resumo da aula</div>", CARIMBO_FIXO, CARIMBO_FIXO);

	private static final List<AnotacaoResponse> ANOTACOES_FIXAS = List.of(ANOTACAO_FIXA, ANOTACAO_FIXA_2);

	private static final List<CategoriaSummaryResponse> CATEGORIAS_FIXAS = List.of(
			new CategoriaSummaryResponse(CATEGORIA_ID_FIXO, "Engenharia de Software", "Disciplina do 6º semestre",
					Cor.ROXO, ANOTACOES_FIXAS.size()));

	private static final CategoriaResponse CATEGORIA_FIXA = new CategoriaResponse(CATEGORIA_ID_FIXO,
			"Engenharia de Software", "Disciplina do 6º semestre", Cor.ROXO, ANOTACOES_FIXAS);

	@GetMapping
	public List<CategoriaSummaryResponse> listar() {
		return CATEGORIAS_FIXAS;
	}

	@GetMapping("/{id}")
	public CategoriaResponse buscarPorId(@PathVariable String id) {
		return CATEGORIA_FIXA;
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaCreateRequest request) {
		CategoriaResponse categoria = new CategoriaResponse(CATEGORIA_ID_FIXO, request.nome(), request.descricao(),
				request.cor(), List.of());

		URI local = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(categoria.id())
				.toUri();

		return ResponseEntity.created(local).body(categoria);
	}

	@PutMapping("/{id}")
	public CategoriaResponse atualizar(@PathVariable String id, @Valid @RequestBody CategoriaUpdateRequest request) {
		return new CategoriaResponse(id, request.nome(), request.descricao(), request.cor(), ANOTACOES_FIXAS);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable String id) {
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
