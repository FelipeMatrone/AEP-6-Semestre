package br.com.cesumar.aep.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tarefas")
public class Tarefa {

	@Id
	private String id;

	private String titulo;

	private LocalDate prazo;

	private Prioridade prioridade;

	private boolean concluida;

	private String dono;

	private Instant criadaEm;

	private Instant atualizadaEm;

	public Tarefa() {
	}

	// os quatro campos que chegam do cliente. concluida, criadaEm e atualizadaEm
	// são derivados e nascem no Service.
	public Tarefa(String titulo, LocalDate prazo, Prioridade prioridade, String dono) {
		this.titulo = titulo;
		this.prazo = prazo;
		this.prioridade = prioridade;
		this.dono = dono;
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

	public LocalDate getPrazo() {
		return prazo;
	}

	public void setPrazo(LocalDate prazo) {
		this.prazo = prazo;
	}

	public Prioridade getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Prioridade prioridade) {
		this.prioridade = prioridade;
	}

	public boolean isConcluida() {
		return concluida;
	}

	public void setConcluida(boolean concluida) {
		this.concluida = concluida;
	}

	public String getDono() {
		return dono;
	}

	public void setDono(String dono) {
		this.dono = dono;
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
