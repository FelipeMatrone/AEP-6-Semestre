package br.com.cesumar.aep.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

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

	// por padrão o conversor grava um campo _class com o nome da classe Java.
	// O $jsonSchema da coleção é fechado (additionalProperties: false), então
	// esse campo extra faz TODO insert falhar com "Document failed validation".
	// Passar null ao DefaultMongoTypeMapper desliga a escrita do tipo; o
	// mapeamento de volta continua funcionando porque a classe alvo é conhecida
	// pelo repositório. Redefinir o bean inteiro é o preço de o Boot só o criar
	// quando não existe outro — daí ter de repetir a fiação dele aqui.
	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext contexto,
			MongoCustomConversions conversoes) {
		DbRefResolver resolvedor = new DefaultDbRefResolver(factory);

		MappingMongoConverter conversor = new MappingMongoConverter(resolvedor, contexto);
		conversor.setTypeMapper(new DefaultMongoTypeMapper(null));
		conversor.setCustomConversions(conversoes);

		return conversor;
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
