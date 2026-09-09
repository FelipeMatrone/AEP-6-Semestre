package br.com.cesumar.aep.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String CORPO_INVALIDO = "Corpo da requisição inválido ou mal formado";

	@ExceptionHandler(TarefaNotFoundException.class)
	public ResponseEntity<ApiError> tarefaNaoEncontrada(TarefaNotFoundException excecao, HttpServletRequest requisicao) {
		return resposta(HttpStatus.NOT_FOUND, excecao.getMessage(), requisicao, Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> corpoInvalido(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
		Map<String, String> erros = new LinkedHashMap<>();
		for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
			erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
		}

		return resposta(HttpStatus.BAD_REQUEST, CORPO_INVALIDO, requisicao, erros);
	}

	// cobre JSON mal formado e slug de prioridade desconhecido, que falha na
	// desserialização antes de o Bean Validation rodar.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> corpoIlegivel(HttpMessageNotReadableException excecao, HttpServletRequest requisicao) {
		return resposta(HttpStatus.BAD_REQUEST, CORPO_INVALIDO, requisicao, Map.of());
	}

	private ResponseEntity<ApiError> resposta(HttpStatus status, String mensagem, HttpServletRequest requisicao,
			Map<String, String> fieldErrors) {
		ApiError erro = new ApiError(
				status.value(),
				status.getReasonPhrase(),
				mensagem,
				requisicao.getRequestURI(),
				fieldErrors);

		return ResponseEntity.status(status).body(erro);
	}

}
