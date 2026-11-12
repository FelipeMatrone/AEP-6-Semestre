package br.com.cesumar.aep.exception;

public class UsuarioNotFoundException extends RecursoNaoEncontradoException {

	public UsuarioNotFoundException(String id) {
		super("Usuário não encontrado: " + id);
	}

}
