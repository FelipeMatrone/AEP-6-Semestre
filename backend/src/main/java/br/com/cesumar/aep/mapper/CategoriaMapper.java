package br.com.cesumar.aep.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.cesumar.aep.dto.AnotacaoCreateRequest;
import br.com.cesumar.aep.dto.AnotacaoResponse;
import br.com.cesumar.aep.dto.AnotacaoUpdateRequest;
import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaResponse;
import br.com.cesumar.aep.dto.CategoriaSummaryResponse;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;

@Component
public class CategoriaMapper {

	// traduz apenas o que veio do cliente. usuarioId, criadaEm e atualizadaEm
	// ficam como estão — quem os define é o Service.
	public Categoria toModel(CategoriaCreateRequest request) {
		return new Categoria(request.nome(), request.descricao(), request.cor());
	}

	// atualiza a instância persistida no lugar, o que preserva id, criadaEm e
	// anotacoes.
	public void updateModel(Categoria categoria, CategoriaUpdateRequest request) {
		categoria.setNome(request.nome());
		categoria.setDescricao(request.descricao());
		categoria.setCor(request.cor());
	}

	public CategoriaResponse toResponse(Categoria categoria) {
		List<AnotacaoResponse> anotacoes = categoria.getAnotacoes().stream()
				.map(this::toAnotacaoResponse)
				.toList();

		return new CategoriaResponse(
				categoria.getId(),
				categoria.getNome(),
				categoria.getDescricao(),
				categoria.getCor(),
				anotacoes);
	}

	// totalAnotacoes nasce do tamanho da lista, nunca de campo guardado.
	public CategoriaSummaryResponse toSummaryResponse(Categoria categoria) {
		return new CategoriaSummaryResponse(
				categoria.getId(),
				categoria.getNome(),
				categoria.getDescricao(),
				categoria.getCor(),
				categoria.getAnotacoes().size());
	}

	// id, criadaEm e atualizadaEm nascem no Service.
	public Anotacao toAnotacaoModel(AnotacaoCreateRequest request) {
		return new Anotacao(request.titulo(), request.conteudo());
	}

	public void updateAnotacao(Anotacao anotacao, AnotacaoUpdateRequest request) {
		anotacao.setTitulo(request.titulo());
		anotacao.setConteudo(request.conteudo());
	}

	public AnotacaoResponse toAnotacaoResponse(Anotacao anotacao) {
		return new AnotacaoResponse(
				anotacao.getId(),
				anotacao.getTitulo(),
				anotacao.getConteudo(),
				anotacao.getCriadaEm(),
				anotacao.getAtualizadaEm());
	}

}
