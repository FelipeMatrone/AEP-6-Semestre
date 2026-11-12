package br.com.cesumar.aep.exception;

public class SenhaAtualIncorretaException extends RuntimeException {

	public SenhaAtualIncorretaException() {
		super("A senha atual não confere");
	}

}
