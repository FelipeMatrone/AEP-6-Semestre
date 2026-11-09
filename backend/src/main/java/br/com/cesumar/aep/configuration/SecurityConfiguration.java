package br.com.cesumar.aep.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// A cadeia de filtros (SecurityFilterChain) entra neste mesmo arquivo na
// Tarefa 8. Por ora só o encoder: sem este bean, qualquer @SpringBootTest
// derruba o contexto, porque UsuarioService pede um PasswordEncoder.
@Configuration
public class SecurityConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
