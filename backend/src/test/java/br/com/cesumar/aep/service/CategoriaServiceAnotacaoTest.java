package br.com.cesumar.aep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.cesumar.aep.dto.AnotacaoCreateRequest;
import br.com.cesumar.aep.dto.AnotacaoResponse;
import br.com.cesumar.aep.dto.AnotacaoUpdateRequest;
import br.com.cesumar.aep.exception.AnotacaoNotFoundException;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.mapper.CategoriaMapper;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;
import br.com.cesumar.aep.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceAnotacaoTest {

	private static final Instant AGORA = Instant.parse("2026-11-08T12:00:00Z");

	private static final Instant ONTEM = Instant.parse("2026-11-07T12:00:00Z");

	private static final String USUARIO = "672f0000000000000000a001";

	private static final String ID_ANOTACAO = "672f0000000000000000c001";

	@Mock
	private CategoriaRepository repository;

	private CategoriaService service;

	@BeforeEach
	void prepararService() {
		service = new CategoriaService(repository, new CategoriaMapper(), Clock.fixed(AGORA, ZoneOffset.UTC));
	}

	@Test
	void criarAnotacaoGeraIdHexadecimalDe24Caracteres() {
		Categoria categoria = categoria("1");
		when(repository.findById("1")).thenReturn(Optional.of(categoria));

		AnotacaoResponse resposta = service.criarAnotacao("1",
				new AnotacaoCreateRequest("Resumo", "<div>texto</div>"), USUARIO);

		assertThat(resposta.id()).matches("[0-9a-f]{24}");
		assertThat(resposta.criadaEm()).isEqualTo(AGORA);
		assertThat(resposta.atualizadaEm()).isEqualTo(AGORA);
		assertThat(categoria.getAnotacoes()).extracting(Anotacao::getId).containsExactly(resposta.id());
		verify(repository).save(categoria);
	}

	@Test
	void criarAnotacaoTocaAtualizadaEmDaCategoria() {
		Categoria categoria = categoria("1");
		when(repository.findById("1")).thenReturn(Optional.of(categoria));

		service.criarAnotacao("1", new AnotacaoCreateRequest("Resumo", "<div>texto</div>"), USUARIO);

		assertThat(categoria.getAtualizadaEm()).isEqualTo(AGORA);
		assertThat(categoria.getCriadaEm()).isEqualTo(ONTEM);
	}

	@Test
	void atualizarAnotacaoPreservaIdECriadaEm() {
		Categoria categoria = categoria("1");
		categoria.getAnotacoes().add(anotacao(ID_ANOTACAO));
		when(repository.findById("1")).thenReturn(Optional.of(categoria));

		AnotacaoResponse resposta = service.atualizarAnotacao("1", ID_ANOTACAO,
				new AnotacaoUpdateRequest("Novo", "<div>novo</div>"), USUARIO);

		assertThat(resposta.id()).isEqualTo(ID_ANOTACAO);
		assertThat(resposta.titulo()).isEqualTo("Novo");
		assertThat(resposta.criadaEm()).isEqualTo(ONTEM);
		assertThat(resposta.atualizadaEm()).isEqualTo(AGORA);
		assertThat(categoria.getAtualizadaEm()).isEqualTo(AGORA);
	}

	// o id existe, mas em outra categoria: tem de falhar como não encontrada.
	@Test
	void atualizarAnotacaoDeOutraCategoriaFalhaComoNaoEncontrada() {
		Categoria alvo = categoria("1");
		Categoria outra = categoria("2");
		outra.getAnotacoes().add(anotacao(ID_ANOTACAO));
		when(repository.findById("1")).thenReturn(Optional.of(alvo));

		assertThatThrownBy(() -> service.atualizarAnotacao("1", ID_ANOTACAO,
				new AnotacaoUpdateRequest("Novo", "<div>novo</div>"), USUARIO))
				.isInstanceOf(AnotacaoNotFoundException.class)
				.hasMessageContaining(ID_ANOTACAO);

		verify(repository, never()).save(any(Categoria.class));
	}

	@Test
	void excluirUltimaAnotacaoDeixaListaVaziaNaoNula() {
		Categoria categoria = categoria("1");
		categoria.getAnotacoes().add(anotacao(ID_ANOTACAO));
		when(repository.findById("1")).thenReturn(Optional.of(categoria));

		service.excluirAnotacao("1", ID_ANOTACAO, USUARIO);

		assertThat(categoria.getAnotacoes()).isNotNull().isEmpty();
		assertThat(categoria.getAtualizadaEm()).isEqualTo(AGORA);
		verify(repository).save(categoria);
	}

	@Test
	void excluirAnotacaoInexistenteFalhaSemSalvar() {
		when(repository.findById("1")).thenReturn(Optional.of(categoria("1")));

		assertThatThrownBy(() -> service.excluirAnotacao("1", ID_ANOTACAO, USUARIO))
				.isInstanceOf(AnotacaoNotFoundException.class);

		verify(repository, never()).save(any(Categoria.class));
	}

	@Test
	void criarAnotacaoEmCategoriaDeOutroUsuarioFalhaComoNaoEncontrada() {
		Categoria alheia = categoria("1");
		alheia.setUsuarioId("672f0000000000000000a002");
		when(repository.findById("1")).thenReturn(Optional.of(alheia));

		assertThatThrownBy(() -> service.criarAnotacao("1",
				new AnotacaoCreateRequest("Resumo", "<div>texto</div>"), USUARIO))
				.isInstanceOf(CategoriaNotFoundException.class);

		verify(repository, never()).save(any(Categoria.class));
	}

	private Categoria categoria(String id) {
		Categoria categoria = new Categoria("Engenharia", "Disciplina", Cor.ROXO);
		categoria.setId(id);
		categoria.setUsuarioId(USUARIO);
		categoria.setCriadaEm(ONTEM);
		categoria.setAtualizadaEm(ONTEM);
		return categoria;
	}

	private Anotacao anotacao(String id) {
		Anotacao anotacao = new Anotacao("Antigo", "<div>antigo</div>");
		anotacao.setId(id);
		anotacao.setCriadaEm(ONTEM);
		anotacao.setAtualizadaEm(ONTEM);
		return anotacao;
	}

}
