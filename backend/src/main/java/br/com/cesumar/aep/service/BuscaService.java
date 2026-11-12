package br.com.cesumar.aep.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.BuscaAnotacaoResponse;
import br.com.cesumar.aep.dto.BuscaCategoriaResponse;
import br.com.cesumar.aep.dto.BuscaResponse;
import br.com.cesumar.aep.dto.BuscaTarefaResponse;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.repository.CategoriaRepository;
import br.com.cesumar.aep.repository.TarefaRepository;

@Service
public class BuscaService {

	private static final int LIMITE_POR_TIPO = 5;

	private final TarefaRepository tarefaRepository;

	private final CategoriaRepository categoriaRepository;

	public BuscaService(TarefaRepository tarefaRepository, CategoriaRepository categoriaRepository) {
		this.tarefaRepository = tarefaRepository;
		this.categoriaRepository = categoriaRepository;
	}

	public BuscaResponse buscar(String termo, String usuarioId) {
		String termoNormalizado = termo == null ? "" : termo.trim().toLowerCase(Locale.ROOT);
		if (termoNormalizado.isEmpty()) {
			return new BuscaResponse(List.of(), List.of(), List.of());
		}

		List<Categoria> categorias = categoriaRepository.findByUsuarioId(usuarioId);
		List<BuscaTarefaResponse> tarefas = tarefaRepository.findByUsuarioId(usuarioId).stream()
				.filter(tarefa -> contem(tarefa.getTitulo(), termoNormalizado))
				.limit(LIMITE_POR_TIPO)
				.map(tarefa -> new BuscaTarefaResponse(tarefa.getId(), tarefa.getTitulo(), tarefa.getCategoriaId()))
				.toList();
		List<BuscaCategoriaResponse> categoriasEncontradas = categorias.stream()
				.filter(categoria -> contem(categoria.getNome(), termoNormalizado))
				.limit(LIMITE_POR_TIPO)
				.map(categoria -> new BuscaCategoriaResponse(categoria.getId(), categoria.getNome()))
				.toList();
		List<BuscaAnotacaoResponse> anotacoes = categorias.stream()
				.flatMap(categoria -> categoria.getAnotacoes().stream()
						.filter(anotacao -> contem(anotacao.getTitulo(), termoNormalizado)
								|| contem(anotacao.getConteudo(), termoNormalizado))
						.map(anotacao -> new BuscaAnotacaoResponse(categoria.getId(), categoria.getNome(),
								anotacao.getId(), anotacao.getTitulo())))
				.limit(LIMITE_POR_TIPO)
				.toList();

		return new BuscaResponse(tarefas, categoriasEncontradas, anotacoes);
	}

	private boolean contem(String texto, String termo) {
		return texto != null && texto.toLowerCase(Locale.ROOT).contains(termo);
	}
}
