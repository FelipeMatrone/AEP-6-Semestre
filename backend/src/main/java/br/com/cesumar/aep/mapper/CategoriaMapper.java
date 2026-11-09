package br.com.cesumar.aep.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.cesumar.aep.dto.AnotacaoResponse;
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

	public AnotacaoResponse toAnotacaoResponse(Anotacao anotacao) {
		return new AnotacaoResponse(
				anotacao.getId(),
				anotacao.getTitulo(),
				anotacao.getConteudo(),
				anotacao.getCriadaEm(),
				anotacao.getAtualizadaEm());
	}

}
