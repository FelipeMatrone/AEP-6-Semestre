package br.com.cesumar.aep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Prioridade {

	ALTA("alta"),
	MEDIA("media"),
	BAIXA("baixa");

	private final String slug;

	Prioridade(String slug) {
		this.slug = slug;
	}

	// o contrato HTTP e o banco usam o slug ASCII sem acento; o rótulo acentuado
	// é da tela. Acento no dado propaga risco de normalização Unicode.
	@JsonValue
	public String getSlug() {
		return slug;
	}

	// slug desconhecido vira HttpMessageNotReadableException, tratada como 400
	// pelo GlobalExceptionHandler — não exige validação própria no DTO.
	@JsonCreator
	public static Prioridade porSlug(String slug) {
		for (Prioridade prioridade : values()) {
			if (prioridade.slug.equals(slug)) {
				return prioridade;
			}
		}
		throw new IllegalArgumentException("Prioridade inválida: " + slug);
	}

}
