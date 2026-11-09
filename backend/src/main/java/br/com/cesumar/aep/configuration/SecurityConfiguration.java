package br.com.cesumar.aep.configuration;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
			throws Exception {
		// Handler sem máscara: o valor do cookie XSRF-TOKEN é o mesmo que o
		// frontend devolve no cabeçalho. Custa a proteção BREACH (o XOR por
		// requisição) em troca de o SPA não precisar decodificar nada.
		CsrfTokenRequestAttributeHandler csrf = new CsrfTokenRequestAttributeHandler();
		csrf.setCsrfRequestAttributeName(null);

		return http
				.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(csrf))
				// o token é preguiçoso: o CookieCsrfTokenRepository só grava o
				// cookie quando alguém lê o valor, e ninguém lê numa resposta
				// 401. Este filtro lê, e é por isso que o XSRF-TOKEN chega ao
				// navegador já na primeira requisição.
				.addFilterAfter((requisicao, resposta, cadeia) -> {
					((CsrfToken) requisicao.getAttribute(CsrfToken.class.getName())).getToken();
					cadeia.doFilter(requisicao, resposta);
				}, CsrfFilter.class)
				.securityContext(c -> c.securityContextRepository(securityContextRepository))
				.authorizeHttpRequests(a -> a
						.requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/sessao").permitAll()
						.requestMatchers("/docs/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.anyRequest().authenticated())
				// 401 direto em vez do 302 para a tela de login do Boot: esta
				// API não tem tela.
				.exceptionHandling(e -> e.authenticationEntryPoint(
						new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.build();
	}

	// é a composição padrão da cadeia, declarada como bean só porque o
	// POST /api/sessao autentica à mão e precisa gravar o contexto na mesma
	// implementação que o filtro lê na requisição seguinte.
	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new DelegatingSecurityContextRepository(
				new RequestAttributeSecurityContextRepository(),
				new HttpSessionSecurityContextRepository());
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
		return configuracao.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
