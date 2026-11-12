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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.cesumar.aep.dto.PerfilResponse;
import br.com.cesumar.aep.dto.PerfilUpdateRequest;
import br.com.cesumar.aep.dto.SenhaUpdateRequest;
import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.exception.EmailJaCadastradoException;
import br.com.cesumar.aep.exception.SenhaAtualIncorretaException;
import br.com.cesumar.aep.exception.UsuarioNotFoundException;
import br.com.cesumar.aep.mapper.UsuarioMapper;
import br.com.cesumar.aep.model.Usuario;
import br.com.cesumar.aep.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-08T12:00:00Z");

	private static final String ID = "672f0000000000000000a001";

	private static final String SENHA = "senha-segura-1";

	private static final String HASH = "$2a$10$ovKMhIe6J2sLr3tDhBZrTO/SQQ2nxYqCa7cgeTN.GEr9YMBQ.qSTS";

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
	void criarDefineMetaSemanalPadrao() {
		when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		service.criar(request("ana@aep.com"));

		verify(repository).save(argThat(usuario -> usuario.getMetaSemanal() == 5));
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

	@Test
	void loadUserByUsernameDevolvePrincipalComOIdComoUsername() {
		when(repository.findByEmail("ana@aep.com")).thenReturn(Optional.of(persistido()));

		UserDetails principal = service.loadUserByUsername("ana@aep.com");

		// o id no lugar do username é o que faz Authentication.getName() já ser
		// o usuarioId que os Services comparam para decidir pertencimento.
		assertThat(principal.getUsername()).isEqualTo(ID);
		assertThat(principal.getPassword()).isEqualTo(HASH);
		assertThat(principal.getAuthorities()).isEmpty();
		assertThat(principal).isEqualTo(new UsuarioAutenticado(ID, "Ana", "ana@aep.com", HASH));
		// o hash não vaza para log nenhum que imprima o principal
		assertThat(principal.toString()).doesNotContain(HASH);
	}

	@Test
	void loadUserByUsernameNormalizaEmailParaMinusculas() {
		when(repository.findByEmail("ana@aep.com")).thenReturn(Optional.of(persistido()));

		// sem a normalização aqui, quem digita o e-mail com maiúsculas não entra:
		// o valor gravado é sempre minúsculo.
		assertThat(service.loadUserByUsername("Ana@AEP.com").getUsername()).isEqualTo(ID);
	}

	@Test
	void loadUserByUsernameComEmailDesconhecidoLancaUsernameNotFound() {
		when(repository.findByEmail("ana@aep.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("ANA@aep.com"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("ana@aep.com");
	}

	@Test
	void perfilDevolveMetaSemanalECriadoEm() {
		Usuario usuario = persistido();
		usuario.setMetaSemanal(7);
		usuario.setCriadoEm(AGORA);
		when(repository.findById(ID)).thenReturn(Optional.of(usuario));

		assertThat(service.perfil(ID)).isEqualTo(new PerfilResponse(ID, "Ana", "ana@aep.com", 7, AGORA));
	}

	@Test
	void perfilSemMetaGravadaUsaOPadrao() {
		Usuario usuario = persistido();
		usuario.setMetaSemanal(0);
		when(repository.findById(ID)).thenReturn(Optional.of(usuario));

		assertThat(service.perfil(ID).metaSemanal()).isEqualTo(5);
	}

	@Test
	void perfilDeUsuarioInexistenteLancaNaoEncontrado() {
		when(repository.findById(ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.perfil(ID)).isInstanceOf(UsuarioNotFoundException.class);
	}

	@Test
	void atualizarPerfilGravaNomeSemEspacosEMeta() {
		when(repository.findById(ID)).thenReturn(Optional.of(persistido()));
		when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		PerfilResponse resposta = service.atualizarPerfil(ID, new PerfilUpdateRequest("  Ana Maria ", 10));

		assertThat(resposta.nome()).isEqualTo("Ana Maria");
		assertThat(resposta.metaSemanal()).isEqualTo(10);
		// o e-mail não é editável por aqui: segue o mesmo de antes
		assertThat(resposta.email()).isEqualTo("ana@aep.com");
	}

	@Test
	void trocarSenhaComSenhaAtualCorretaGravaNovoHash() {
		Usuario usuario = persistido();
		usuario.setSenhaHash(encoder.encode(SENHA));
		when(repository.findById(ID)).thenReturn(Optional.of(usuario));

		service.trocarSenha(ID, new SenhaUpdateRequest(SENHA, "nova-senha-2026"));

		verify(repository).save(argThat(salvo -> encoder.matches("nova-senha-2026", salvo.getSenhaHash())));
	}

	@Test
	void trocarSenhaComSenhaAtualErradaNaoGrava() {
		Usuario usuario = persistido();
		usuario.setSenhaHash(encoder.encode(SENHA));
		when(repository.findById(ID)).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> service.trocarSenha(ID, new SenhaUpdateRequest("errada-123", "nova-senha-2026")))
				.isInstanceOf(SenhaAtualIncorretaException.class);

		verify(repository, never()).save(any(Usuario.class));
	}

	private UsuarioCreateRequest request(String email) {
		return new UsuarioCreateRequest("Ana", email, SENHA);
	}

	private Usuario comId(Usuario usuario) {
		usuario.setId(ID);
		return usuario;
	}

	private Usuario persistido() {
		Usuario usuario = comId(new Usuario("Ana", "ana@aep.com"));
		usuario.setSenhaHash(HASH);
		return usuario;
	}

}
