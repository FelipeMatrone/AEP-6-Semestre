package br.com.cesumar.aep.mapper;

import org.springframework.stereotype.Component;

import br.com.cesumar.aep.dto.EventoCalendarioCreateRequest;
import br.com.cesumar.aep.dto.EventoCalendarioResponse;
import br.com.cesumar.aep.dto.EventoCalendarioUpdateRequest;
import br.com.cesumar.aep.model.EventoCalendario;

@Component
public class EventoCalendarioMapper {

	public EventoCalendario toModel(EventoCalendarioCreateRequest request) {
		EventoCalendario evento = new EventoCalendario();
		evento.setTitulo(request.titulo());
		evento.setDescricao(request.descricao());
		evento.setData(request.data());
		evento.setHorario(request.horario());
		evento.setCor(request.cor());
		return evento;
	}

	public void updateModel(EventoCalendario evento, EventoCalendarioUpdateRequest request) {
		evento.setTitulo(request.titulo());
		evento.setDescricao(request.descricao());
		evento.setData(request.data());
		evento.setHorario(request.horario());
		evento.setCor(request.cor());
		evento.setTarefaId(request.tarefaId());
	}

	public EventoCalendarioResponse toResponse(EventoCalendario evento) {
		return new EventoCalendarioResponse(evento.getId(), evento.getTitulo(), evento.getDescricao(), evento.getData(),
				evento.getHorario(), evento.getCor(), evento.getTarefaId(), evento.getCriadaEm(), evento.getAtualizadaEm());
	}
}
