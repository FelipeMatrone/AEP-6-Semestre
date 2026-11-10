package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.UsuarioCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.exception.EmailJaCadastradoException;
import br.com.cesumar.aep.mapper.UsuarioMapper;
import br.com.cesumar.aep.model.Usuario;
import br.com.cesumar.aep.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

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
		usuario.setCriadoEm(Instant.now(clock));

		return mapper.toResponse(repository.save(usuario));
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
