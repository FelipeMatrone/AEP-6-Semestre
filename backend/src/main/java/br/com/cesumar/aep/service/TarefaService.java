package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.TarefaCreateRequest;
import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.exception.TarefaNotFoundException;
import br.com.cesumar.aep.mapper.TarefaMapper;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.CategoriaRepository;
import br.com.cesumar.aep.repository.TarefaRepository;

@Service
public class TarefaService {

	private final TarefaRepository repository;

	private final CategoriaRepository categoriaRepository;

	private final TarefaMapper mapper;

	private final Clock clock;

	public TarefaService(TarefaRepository repository, CategoriaRepository categoriaRepository, TarefaMapper mapper,
			Clock clock) {
		this.repository = repository;
		this.categoriaRepository = categoriaRepository;
		this.mapper = mapper;
		this.clock = clock;
	}

	public List<TarefaSummaryResponse> listar(String usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream()
				.map(mapper::toSummaryResponse)
				.toList();
	}

	public TarefaResponse buscarPorId(String id, String usuarioId) {
		return mapper.toResponse(buscarOuFalhar(id, usuarioId));
	}

	public TarefaResponse criar(TarefaCreateRequest request, String usuarioId) {
		validarCategoria(request.categoriaId(), usuarioId);
		Tarefa tarefa = mapper.toModel(request);

		Instant agora = Instant.now(clock);
		tarefa.setUsuarioId(usuarioId);
		tarefa.setConcluida(false);
		tarefa.setConcluidaEm(null);
		tarefa.setCriadaEm(agora);
		tarefa.setAtualizadaEm(agora);

		return mapper.toResponse(repository.save(tarefa));
	}

	public TarefaResponse atualizar(String id, TarefaUpdateRequest request, String usuarioId) {
		Tarefa tarefa = buscarOuFalhar(id, usuarioId);
		validarCategoria(request.categoriaId(), usuarioId);
		boolean estavaConcluida = tarefa.isConcluida();

		mapper.updateModel(tarefa, request);
		Instant agora = Instant.now(clock);
		if (!estavaConcluida && tarefa.isConcluida()) {
			tarefa.setConcluidaEm(agora);
		} else if (estavaConcluida && !tarefa.isConcluida()) {
			tarefa.setConcluidaEm(null);
		}
		tarefa.setAtualizadaEm(agora);

		return mapper.toResponse(repository.save(tarefa));
	}

	public void excluir(String id, String usuarioId) {
		repository.delete(buscarOuFalhar(id, usuarioId));
	}

	// tarefa de outro usuário é tratada como inexistente: 404, não 403, para
	// não revelar que o id existe.
	private Tarefa buscarOuFalhar(String id, String usuarioId) {
		return repository.findById(id)
				.filter(tarefa -> usuarioId.equals(tarefa.getUsuarioId()))
				.orElseThrow(() -> new TarefaNotFoundException(id));
	}

	// categoria de outro usuário também é 404 (e não 400): mesma regra de não
	// revelar a existência do id.
	private void validarCategoria(String categoriaId, String usuarioId) {
		if (categoriaId == null) {
			return;
		}
		categoriaRepository.findById(categoriaId)
				.filter(categoria -> usuarioId.equals(categoria.getUsuarioId()))
				.orElseThrow(() -> new CategoriaNotFoundException(categoriaId));
	}

}
