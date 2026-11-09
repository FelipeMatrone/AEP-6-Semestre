package br.com.cesumar.aep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaResponse;
import br.com.cesumar.aep.dto.CategoriaSummaryResponse;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.mapper.CategoriaMapper;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-08T12:00:00Z");

	private static final Instant ONTEM = Instant.parse("2026-11-07T12:00:00Z");

	private static final String USUARIO = "672f0000000000000000a001";

	private static final String OUTRO_USUARIO = "672f0000000000000000a002";

	@Mock
	private CategoriaRepository repository;

	private CategoriaService service;

	@BeforeEach
	void prepararService() {
		// Mapper de verdade e relógio fixo, pelo mesmo motivo de TarefaServiceTest.
		service = new CategoriaService(repository, new CategoriaMapper(), Clock.fixed(AGORA, ZoneOffset.UTC));
	}

	@Test
	void criarDerivaDatasEIniciaListaVazia() {
		when(repository.save(any(Categoria.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		CategoriaResponse resposta = service.criar(
				new CategoriaCreateRequest("Redes", "Disciplina", Cor.VERDE), USUARIO);

		assertThat(resposta.anotacoes()).isEmpty();
		verify(repository).save(argThat(categoria -> USUARIO.equals(categoria.getUsuarioId())
				&& AGORA.equals(categoria.getCriadaEm())
				&& AGORA.equals(categoria.getAtualizadaEm())
				&& categoria.getAnotacoes().isEmpty()));
	}

	@Test
	void listarCalculaTotalDeAnotacoes() {
		Categoria categoria = categoria("1", USUARIO);
		categoria.getAnotacoes().add(new Anotacao("A", "a"));
		categoria.getAnotacoes().add(new Anotacao("B", "b"));
		when(repository.findByUsuarioId(USUARIO)).thenReturn(List.of(categoria));

		List<CategoriaSummaryResponse> resumos = service.listar(USUARIO);

		assertThat(resumos).containsExactly(
				new CategoriaSummaryResponse("1", "Engenharia", "Disciplina", Cor.ROXO, 2));
	}

	@Test
	void buscarCategoriaDeOutroUsuarioFalhaComoNaoEncontrada() {
		when(repository.findById("1")).thenReturn(Optional.of(categoria("1", OUTRO_USUARIO)));

		assertThatThrownBy(() -> service.buscarPorId("1", USUARIO))
				.isInstanceOf(CategoriaNotFoundException.class)
				.hasMessageContaining("1");
	}

	@Test
	void atualizarPreservaCriadaEmEAnotacoes() {
		Categoria persistida = categoria("1", USUARIO);
		persistida.setCriadaEm(ONTEM);
		persistida.setAtualizadaEm(ONTEM);
		persistida.getAnotacoes().add(new Anotacao("A", "a"));
		when(repository.findById("1")).thenReturn(Optional.of(persistida));
		when(repository.save(persistida)).thenReturn(persistida);

		CategoriaResponse resposta = service.atualizar("1",
				new CategoriaUpdateRequest("Redes", "Nova", Cor.AMBAR), USUARIO);

		assertThat(resposta.nome()).isEqualTo("Redes");
		assertThat(resposta.cor()).isEqualTo(Cor.AMBAR);
		assertThat(resposta.anotacoes()).hasSize(1);
		assertThat(persistida.getCriadaEm()).isEqualTo(ONTEM);
		assertThat(persistida.getAtualizadaEm()).isEqualTo(AGORA);
	}

	@Test
	void excluirCategoriaDeOutroUsuarioNaoApaga() {
		when(repository.findById("1")).thenReturn(Optional.of(categoria("1", OUTRO_USUARIO)));

		assertThatThrownBy(() -> service.excluir("1", USUARIO))
				.isInstanceOf(CategoriaNotFoundException.class);

		verify(repository, never()).delete(any(Categoria.class));
	}

	@Test
	void excluirCategoriaExistenteApagaDoRepositorio() {
		Categoria persistida = categoria("1", USUARIO);
		when(repository.findById("1")).thenReturn(Optional.of(persistida));

		service.excluir("1", USUARIO);

		verify(repository).delete(persistida);
	}

	private Categoria categoria(String id, String usuarioId) {
		Categoria categoria = new Categoria("Engenharia", "Disciplina", Cor.ROXO);
		categoria.setId(id);
		categoria.setUsuarioId(usuarioId);
		categoria.setCriadaEm(AGORA);
		categoria.setAtualizadaEm(AGORA);
		return categoria;
	}

}
