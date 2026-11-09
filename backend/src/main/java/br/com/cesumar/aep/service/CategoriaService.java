package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaResponse;
import br.com.cesumar.aep.dto.CategoriaSummaryResponse;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.mapper.CategoriaMapper;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.repository.CategoriaRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository repository;

	private final CategoriaMapper mapper;

	private final Clock clock;

	public CategoriaService(CategoriaRepository repository, CategoriaMapper mapper, Clock clock) {
		this.repository = repository;
		this.mapper = mapper;
		this.clock = clock;
	}

	public List<CategoriaSummaryResponse> listar(String usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream()
				.map(mapper::toSummaryResponse)
				.toList();
	}

	public CategoriaResponse buscarPorId(String id, String usuarioId) {
		return mapper.toResponse(buscarOuFalhar(id, usuarioId));
	}

	public CategoriaResponse criar(CategoriaCreateRequest request, String usuarioId) {
		Categoria categoria = mapper.toModel(request);

		Instant agora = Instant.now(clock);
		categoria.setUsuarioId(usuarioId);
		categoria.setCriadaEm(agora);
		categoria.setAtualizadaEm(agora);

		return mapper.toResponse(repository.save(categoria));
	}

	public CategoriaResponse atualizar(String id, CategoriaUpdateRequest request, String usuarioId) {
		Categoria categoria = buscarOuFalhar(id, usuarioId);

		mapper.updateModel(categoria, request);
		categoria.setAtualizadaEm(Instant.now(clock));

		return mapper.toResponse(repository.save(categoria));
	}

	public void excluir(String id, String usuarioId) {
		repository.delete(buscarOuFalhar(id, usuarioId));
	}

	// categoria de outro usuário é tratada como inexistente: 404, não 403, para
	// não revelar que o id existe.
	private Categoria buscarOuFalhar(String id, String usuarioId) {
		return repository.findById(id)
				.filter(categoria -> usuarioId.equals(categoria.getUsuarioId()))
				.orElseThrow(() -> new CategoriaNotFoundException(id));
	}

}
