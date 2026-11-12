package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.EventoCalendarioCreateRequest;
import br.com.cesumar.aep.dto.EventoCalendarioResponse;
import br.com.cesumar.aep.dto.EventoCalendarioUpdateRequest;
import br.com.cesumar.aep.exception.EventoCalendarioNotFoundException;
import br.com.cesumar.aep.mapper.EventoCalendarioMapper;
import br.com.cesumar.aep.model.EventoCalendario;
import br.com.cesumar.aep.repository.EventoCalendarioRepository;

@Service
public class EventoCalendarioService {

	private final EventoCalendarioRepository repository;

	private final EventoCalendarioMapper mapper;

	private final Clock clock;

	public EventoCalendarioService(EventoCalendarioRepository repository, EventoCalendarioMapper mapper, Clock clock) {
		this.repository = repository;
		this.mapper = mapper;
		this.clock = clock;
	}

	public List<EventoCalendarioResponse> listar(String usuarioId) {
		return repository.findByUsuarioIdOrderByDataAscHorarioAsc(usuarioId).stream()
				.map(mapper::toResponse)
				.toList();
	}

	public EventoCalendarioResponse criar(EventoCalendarioCreateRequest request, String usuarioId) {
		EventoCalendario evento = mapper.toModel(request);
		Instant agora = Instant.now(clock);
		evento.setUsuarioId(usuarioId);
		evento.setCriadaEm(agora);
		evento.setAtualizadaEm(agora);
		return mapper.toResponse(repository.save(evento));
	}

	public EventoCalendarioResponse atualizar(String id, EventoCalendarioUpdateRequest request, String usuarioId) {
		EventoCalendario evento = buscarOuFalhar(id, usuarioId);
		mapper.updateModel(evento, request);
		evento.setAtualizadaEm(Instant.now(clock));
		return mapper.toResponse(repository.save(evento));
	}

	public void excluir(String id, String usuarioId) {
		repository.delete(buscarOuFalhar(id, usuarioId));
	}

	private EventoCalendario buscarOuFalhar(String id, String usuarioId) {
		return repository.findById(id)
				.filter(evento -> usuarioId.equals(evento.getUsuarioId()))
				.orElseThrow(() -> new EventoCalendarioNotFoundException(id));
	}
}
