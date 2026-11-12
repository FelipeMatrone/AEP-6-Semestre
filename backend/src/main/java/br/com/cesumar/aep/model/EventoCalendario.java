package br.com.cesumar.aep.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventosCalendario")
public class EventoCalendario {

	@Id
	private String id;

	private String usuarioId;

	private String titulo;

	private String descricao;

	private LocalDate data;

	private String horario;

	private Cor cor;

	private String tarefaId;

	private Instant criadaEm;

	private Instant atualizadaEm;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUsuarioId() { return usuarioId; }
	public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
	public String getTitulo() { return titulo; }
	public void setTitulo(String titulo) { this.titulo = titulo; }
	public String getDescricao() { return descricao; }
	public void setDescricao(String descricao) { this.descricao = descricao; }
	public LocalDate getData() { return data; }
	public void setData(LocalDate data) { this.data = data; }
	public String getHorario() { return horario; }
	public void setHorario(String horario) { this.horario = horario; }
	public Cor getCor() { return cor; }
	public void setCor(Cor cor) { this.cor = cor; }
	public String getTarefaId() { return tarefaId; }
	public void setTarefaId(String tarefaId) { this.tarefaId = tarefaId; }
	public Instant getCriadaEm() { return criadaEm; }
	public void setCriadaEm(Instant criadaEm) { this.criadaEm = criadaEm; }
	public Instant getAtualizadaEm() { return atualizadaEm; }
	public void setAtualizadaEm(Instant atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}
