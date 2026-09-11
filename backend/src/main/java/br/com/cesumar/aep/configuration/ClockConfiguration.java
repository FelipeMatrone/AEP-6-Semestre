package br.com.cesumar.aep.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {

	// injetar o relógio em vez de chamar Instant.now() no Service torna
	// criadaEm e atualizadaEm verificáveis com valor exato no teste, em vez de
	// "não é nulo". Duas chamadas seguidas a Instant.now() podem devolver o
	// mesmo valor no Windows, o que deixaria o teste de atualizadaEm instável.
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

}
