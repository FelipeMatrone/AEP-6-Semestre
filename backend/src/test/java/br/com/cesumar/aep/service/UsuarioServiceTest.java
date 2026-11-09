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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.exception.EmailJaCadastradoException;
import br.com.cesumar.aep.mapper.UsuarioMapper;
import br.com.cesumar.aep.model.Usuario;
import br.com.cesumar.aep.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-08T12:00:00Z");

	private static final String ID = "672f0000000000000000a001";

	private static final String SENHA = "senha-segura-1";

	// encoder de verdade, não mock: com um mock devolvendo uma string qualquer,
	// criarGuardaSenhaEmHashENuncaEmTextoPuro passaria sem haver hash nenhum.
	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Mock
	private UsuarioRepository repository;

	private UsuarioService service;

	@BeforeEach
	void prepararService() {
		service = new UsuarioService(repository, encoder, new UsuarioMapper(), Clock.fixed(AGORA, ZoneOffset.UTC));
	}

	@Test
	void criarGuardaSenhaEmHashENuncaEmTextoPuro() {
		when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		service.criar(request("ana@aep.com"));

		verify(repository).save(argThat(usuario -> !SENHA.equals(usuario.getSenhaHash())
				&& encoder.matches(SENHA, usuario.getSenhaHash())
				&& AGORA.equals(usuario.getCriadoEm())));
	}

	@Test
	void criarNaoExpoeSenhaHashNaResposta() {
		when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> comId(invocacao.getArgument(0)));

		UsuarioResponse resposta = service.criar(request("ana@aep.com"));

		// a igualdade com o record inteiro é o que prova a ausência: nenhum
		// campo além destes três existe para carregar o hash.
		assertThat(resposta).isEqualTo(new UsuarioResponse(ID, "Ana", "ana@aep.com"));
	}

	@Test
	void criarNormalizaEmailParaMinusculas() {
		when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		UsuarioResponse resposta = service.criar(request("Ana@AEP.com"));

		assertThat(resposta.email()).isEqualTo("ana@aep.com");
		// a checagem de duplicidade usa o mesmo valor normalizado que é gravado,
		// senão duas grafias do mesmo e-mail passam pelo existsByEmail.
		verify(repository).existsByEmail("ana@aep.com");
		verify(repository).save(argThat(usuario -> "ana@aep.com".equals(usuario.getEmail())));
	}

	@Test
	void criarComEmailJaCadastradoLancaEmailJaCadastrado() {
		when(repository.existsByEmail("ana@aep.com")).thenReturn(true);

		assertThatThrownBy(() -> service.criar(request("Ana@AEP.com")))
				.isInstanceOf(EmailJaCadastradoException.class)
				.hasMessageContaining("ana@aep.com");

		verify(repository, never()).save(any(Usuario.class));
	}

	private UsuarioCreateRequest request(String email) {
		return new UsuarioCreateRequest("Ana", email, SENHA);
	}

	private Usuario comId(Usuario usuario) {
		usuario.setId(ID);
		return usuario;
	}

}
