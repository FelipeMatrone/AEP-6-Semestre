package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

@Service
public class UsuarioService implements UserDetailsService {

	private static final int META_SEMANAL_PADRAO = 5;

	private final UsuarioRepository repository;

	private final PasswordEncoder passwordEncoder;

	private final UsuarioMapper mapper;

	private final Clock clock;

	public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder, UsuarioMapper mapper,
			Clock clock) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.mapper = mapper;
		this.clock = clock;
	}

	public UsuarioResponse criar(UsuarioCreateRequest request) {
		// normalizado antes de checar e antes de gravar: normalizar só na
		// gravação deixaria "Ana@AEP.com" passar pelo existsByEmail de
		// "ana@aep.com" e bater no índice único só no insert.
		String email = request.email().toLowerCase(Locale.ROOT);
		if (repository.existsByEmail(email)) {
			throw new EmailJaCadastradoException(email);
		}

		Usuario usuario = mapper.toModel(request);
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		usuario.setMetaSemanal(META_SEMANAL_PADRAO);
		usuario.setCriadoEm(Instant.now(clock));

		return mapper.toResponse(repository.save(usuario));
	}

	public PerfilResponse perfil(String id) {
		return mapper.toPerfil(buscar(id));
	}

	public PerfilResponse atualizarPerfil(String id, PerfilUpdateRequest request) {
		Usuario usuario = buscar(id);
		usuario.setNome(request.nome().trim());
		usuario.setMetaSemanal(request.metaSemanal());

		return mapper.toPerfil(repository.save(usuario));
	}

	public void trocarSenha(String id, SenhaUpdateRequest request) {
		Usuario usuario = buscar(id);
		if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
			throw new SenhaAtualIncorretaException();
		}

		usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
		repository.save(usuario);
	}

	private Usuario buscar(String id) {
		Usuario usuario = repository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
		// conta da 1ª entrega vem sem meta (0): usa o mesmo padrão do painel,
		// senão o perfil mostraria "x/0" e o formulário nasceria inválido.
		if (usuario.getMetaSemanal() <= 0) {
			usuario.setMetaSemanal(META_SEMANAL_PADRAO);
		}
		return usuario;
	}

	// contrato do UserDetailsService: o que chega aqui é o e-mail digitado no
	// login. A normalização repete a de criar porque é o valor gravado que o
	// índice único e o findByEmail conhecem — sem ela "DEMO@aep.local" não entra.
	@Override
	public UserDetails loadUserByUsername(String email) {
		String normalizado = email.toLowerCase(Locale.ROOT);

		return repository.findByEmail(normalizado)
				.map(usuario -> new UsuarioAutenticado(usuario.getId(), usuario.getNome(), usuario.getEmail(),
						usuario.getSenhaHash()))
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + normalizado));
	}

}
