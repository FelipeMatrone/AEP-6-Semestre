package br.com.cesumar.aep.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import br.com.cesumar.aep.model.Prioridade;

@Configuration
public class MongoConfiguration {

	// sem estes conversores o Spring Data grava o nome da constante ("BAIXA"),
	// porque @JsonValue só age na fronteira HTTP. O banco guarda o slug ASCII,
	// que é o que o $jsonSchema da coleção valida.
	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(List.of(new PrioridadeParaSlug(), new SlugParaPrioridade()));
	}

	@WritingConverter
	static class PrioridadeParaSlug implements Converter<Prioridade, String> {

		@Override
		public String convert(Prioridade prioridade) {
			return prioridade.getSlug();
		}

	}

	@ReadingConverter
	static class SlugParaPrioridade implements Converter<String, Prioridade> {

		@Override
		public Prioridade convert(String slug) {
			return Prioridade.porSlug(slug);
		}

	}

}
