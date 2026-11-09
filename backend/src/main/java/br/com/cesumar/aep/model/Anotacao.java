package br.com.cesumar.aep.model;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Field;

// subdocumento da Categoria. O Mongo não gera id para elemento de array, então
// o Service grava um ObjectId em hexadecimal como string. O @Field evita que o
// Spring Data trate o campo como o _id do subdocumento.
public class Anotacao {

	@Field("id")
	private String id;

	private String titulo;

	private String conteudo;

	private Instant criadaEm;

	private Instant atualizadaEm;

	public Anotacao() {
	}

	// os dois campos que chegam do cliente. id, criadaEm e atualizadaEm são
	// derivados e nascem no Service.
	public Anotacao(String titulo, String conteudo) {
		this.titulo = titulo;
		this.conteudo = conteudo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public Instant getCriadaEm() {
		return criadaEm;
	}

	public void setCriadaEm(Instant criadaEm) {
		this.criadaEm = criadaEm;
	}

	public Instant getAtualizadaEm() {
		return atualizadaEm;
	}

	public void setAtualizadaEm(Instant atualizadaEm) {
		this.atualizadaEm = atualizadaEm;
	}

}
