package br.com.cesumar.aep;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AepApplication {

	public static void main(String[] args) {
		// prazo é data de calendário gravada à meia-noite UTC. O conversor padrão do
		// Spring Data usa ZoneId.systemDefault(), que gravaria 03:00Z em Maringá e o
		// dia seguinte em qualquer fuso positivo. Fixar o fuso do processo mantém a
		// invariante verdadeira no banco, independentemente da máquina.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(AepApplication.class, args);
	}

}
