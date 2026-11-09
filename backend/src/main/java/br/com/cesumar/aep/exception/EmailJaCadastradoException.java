package br.com.cesumar.aep.exception;

// não estende RecursoNaoEncontradoException: aquele mapeia para 404, e e-mail
// repetido é conflito (409), não recurso ausente.
public class EmailJaCadastradoException extends RuntimeException {

	public EmailJaCadastradoException(String email) {
		super("E-mail já cadastrado: " + email);
	}

}
