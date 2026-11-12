package br.com.cesumar.aep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.cesumar.aep.dto.BuscaResponse;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.model.Prioridade;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.CategoriaRepository;
import br.com.cesumar.aep.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class BuscaServiceTest {

	private static final String USUARIO = "u1";

	@Mock
	private TarefaRepository tarefaRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	private BuscaService service;

	@BeforeEach
	void prepararService() {
		service = new BuscaService(tarefaRepository, categoriaRepository);
	}

	@Test
	void buscarAgrupaTarefasCategoriasEAnotacoesDoUsuario() {
		when(tarefaRepository.findByUsuarioId(USUARIO)).thenReturn(List.of(tarefa("t1", "Estudar MongoDB")));
		when(categoriaRepository.findByUsuarioId(USUARIO)).thenReturn(List.of(categoria()));

		BuscaResponse resposta = service.buscar("mongo", USUARIO);

		assertThat(resposta.tarefas()).extracting(resultado -> resultado.titulo()).containsExactly("Estudar MongoDB");
		assertThat(resposta.categorias()).extracting(resultado -> resultado.nome()).containsExactly("MongoDB");
		assertThat(resposta.anotacoes()).extracting(resultado -> resultado.titulo()).containsExactly("Índices MongoDB");
		assertThat(resposta.anotacoes().get(0).categoriaId()).isEqualTo("c1");
	}

	@Test
	void buscarEmBrancoNaoConsultaOBanco() {
		BuscaResponse resposta = service.buscar("   ", USUARIO);

		assertThat(resposta.tarefas()).isEmpty();
		assertThat(resposta.categorias()).isEmpty();
		assertThat(resposta.anotacoes()).isEmpty();
		verify(tarefaRepository, never()).findByUsuarioId(USUARIO);
		verify(categoriaRepository, never()).findByUsuarioId(USUARIO);
	}

	private Tarefa tarefa(String id, String titulo) {
		Tarefa tarefa = new Tarefa(titulo, null, Prioridade.MEDIA, null);
		tarefa.setId(id);
		return tarefa;
	}

	private Categoria categoria() {
		Categoria categoria = new Categoria("MongoDB", null, Cor.VERDE);
		categoria.setId("c1");
		Anotacao anotacao = new Anotacao("Índices MongoDB", "Índices aceleram consultas.");
		anotacao.setId("a1");
		categoria.setAnotacoes(List.of(anotacao));
		return categoria;
	}
}
