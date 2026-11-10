package br.com.cesumar.aep.exception;

public class CategoriaNotFoundException extends RecursoNaoEncontradoException {

	public CategoriaNotFoundException(String id) {
		super("Categoria não encontrada: " + id);
	}

}
