package br.com.cesumar.aep.exception;

public class EventoCalendarioNotFoundException extends RecursoNaoEncontradoException {

	public EventoCalendarioNotFoundException(String id) {
		super("Evento do calendário não encontrado: " + id);
	}
}
