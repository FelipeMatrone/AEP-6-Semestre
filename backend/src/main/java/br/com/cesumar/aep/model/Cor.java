package br.com.cesumar.aep.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Cor {

	ROXO("roxo"),
	MAGENTA("magenta"),
	VERDE("verde"),
	AMBAR("ambar");

	private final String slug;

	Cor(String slug) {
		this.slug = slug;
	}

	// mesmo contrato de Prioridade: slug ASCII minúsculo no HTTP e no banco.
	@JsonValue
	public String getSlug() {
		return slug;
	}

	// slug desconhecido vira HttpMessageNotReadableException, tratada como 400
	// pelo GlobalExceptionHandler.
	@JsonCreator
	public static Cor porSlug(String slug) {
		for (Cor cor : values()) {
			if (cor.slug.equals(slug)) {
				return cor;
			}
		}
		throw new IllegalArgumentException("Cor inválida: " + slug);
	}

}
