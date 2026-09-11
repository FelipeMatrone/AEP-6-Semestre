package br.com.cesumar.aep.exception;

public class TarefaNotFoundException extends RuntimeException {

	public TarefaNotFoundException(String id) {
		super("Tarefa não encontrada: " + id);
	}

}
