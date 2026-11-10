package br.com.cesumar.aep.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias")
public class Categoria {

	@Id
	private String id;

	private String usuarioId;

	private String nome;

	private String descricao;

	private Cor cor;

	private Instant criadaEm;

	private Instant atualizadaEm;

	// nunca nula: a lista nasce vazia e o total de anotações é derivado dela.
	private List<Anotacao> anotacoes = new ArrayList<>();

	public Categoria() {
	}

	// os três campos que chegam do cliente. usuarioId, criadaEm e atualizadaEm
	// são derivados e nascem no Service.
	public Categoria(String nome, String descricao, Cor cor) {
		this.nome = nome;
		this.descricao = descricao;
		this.cor = cor;
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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Cor getCor() {
		return cor;
	}

	public void setCor(Cor cor) {
		this.cor = cor;
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

	public List<Anotacao> getAnotacoes() {
		return anotacoes;
	}

	public void setAnotacoes(List<Anotacao> anotacoes) {
		this.anotacoes = anotacoes;
	}

}
