package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.TarefaCreateRequest;
import br.com.cesumar.aep.dto.TarefaResponse;
import br.com.cesumar.aep.dto.TarefaSummaryResponse;
import br.com.cesumar.aep.dto.TarefaUpdateRequest;
import br.com.cesumar.aep.exception.TarefaNotFoundException;
import br.com.cesumar.aep.mapper.TarefaMapper;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.TarefaRepository;

@Service
public class TarefaService {

	private final TarefaRepository repository;

	private final TarefaMapper mapper;

	private final Clock clock;

	public TarefaService(TarefaRepository repository, TarefaMapper mapper, Clock clock) {
		this.repository = repository;
		this.mapper = mapper;
		this.clock = clock;
	}

	public List<TarefaSummaryResponse> listar() {
		return repository.findAll().stream()
				.map(mapper::toSummaryResponse)
				.toList();
	}

	public TarefaResponse buscarPorId(String id) {
		return mapper.toResponse(buscarOuFalhar(id));
	}

	public TarefaResponse criar(TarefaCreateRequest request) {
		Tarefa tarefa = mapper.toModel(request);

		Instant agora = Instant.now(clock);
		tarefa.setConcluida(false);
		tarefa.setCriadaEm(agora);
		tarefa.setAtualizadaEm(agora);

		return mapper.toResponse(repository.save(tarefa));
	}

	public TarefaResponse atualizar(String id, TarefaUpdateRequest request) {
		Tarefa tarefa = buscarOuFalhar(id);

		mapper.updateModel(tarefa, request);
		tarefa.setAtualizadaEm(Instant.now(clock));

		return mapper.toResponse(repository.save(tarefa));
	}

	public void excluir(String id) {
		repository.delete(buscarOuFalhar(id));
	}

	private Tarefa buscarOuFalhar(String id) {
		return repository.findById(id).orElseThrow(() -> new TarefaNotFoundException(id));
	}

}
