package br.com.cesumar.aep.exception;

import java.util.Map;

// fieldErrors só existe quando a falha é de validação de campo; nos demais
// casos vem vazio, e não nulo, para o cliente não precisar checar.
public record ApiError(
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors) {
}
