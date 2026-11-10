package br.com.cesumar.aep.exception;

public class AnotacaoNotFoundException extends RecursoNaoEncontradoException {

	public AnotacaoNotFoundException(String id) {
		super("Anotação não encontrada: " + id);
	}

}
