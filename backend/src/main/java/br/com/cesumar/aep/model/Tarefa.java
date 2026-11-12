package br.com.cesumar.aep.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tarefas")
public class Tarefa {

	@Id
	private String id;

	// dono da tarefa; nunca sai nas respostas.
	private String usuarioId;

	// opcional: tarefa sem disciplina é válida.
	private String categoriaId;

	private String titulo;

	private LocalDate prazo;

	private Prioridade prioridade;

	private boolean concluida;

	// instante em que a tarefa foi concluída; nulo enquanto estiver pendente.
	private Instant concluidaEm;

	private String observacao;

	private Instant criadaEm;

	private Instant atualizadaEm;

	public Tarefa() {
	}

	// os quatro campos que chegam do cliente. concluida, usuarioId, criadaEm e
	// atualizadaEm são derivados e nascem no Service.
	public Tarefa(String titulo, LocalDate prazo, Prioridade prioridade, String observacao) {
		this.titulo = titulo;
		this.prazo = prazo;
		this.prioridade = prioridade;
		this.observacao = observacao;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(String usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(String categoriaId) {
		this.categoriaId = categoriaId;
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

	public Instant getConcluidaEm() {
		return concluidaEm;
	}

	public void setConcluidaEm(Instant concluidaEm) {
		this.concluidaEm = concluidaEm;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
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
