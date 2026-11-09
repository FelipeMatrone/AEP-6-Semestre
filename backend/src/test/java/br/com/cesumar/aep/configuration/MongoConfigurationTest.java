package br.com.cesumar.aep.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Cor;

class MongoConfigurationTest {

	private final MongoConfiguration configuracao = new MongoConfiguration();

	@Test
	void categoriaPersisteCorEmSlugEAnotacaoComIdString() {
		MongoCustomConversions conversoes = configuracao.mongoCustomConversions();
		MongoMappingContext contexto = new MongoMappingContext();
		contexto.setSimpleTypeHolder(conversoes.getSimpleTypeHolder());
		MappingMongoConverter conversor = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, contexto);
		conversor.setCustomConversions(conversoes);
		conversor.afterPropertiesSet();

		Anotacao anotacao = new Anotacao("Resumo", "<div>texto</div>");
		anotacao.setId("672f0000000000000000c001");
		anotacao.setCriadaEm(Instant.parse("2026-11-08T12:00:00Z"));
		Categoria categoria = new Categoria("Redes", "Disciplina", Cor.AMBAR);
		categoria.setAnotacoes(List.of(anotacao));

		Document documento = new Document();
		conversor.write(categoria, documento);
		Document gravada = documento.getList("anotacoes", Document.class).get(0);

		assertThat(documento.get("cor")).isEqualTo("ambar");
		assertThat(gravada.get("id")).isEqualTo("672f0000000000000000c001");
		assertThat(gravada).doesNotContainKey("_id");

		Categoria lida = conversor.read(Categoria.class, documento);

		assertThat(lida.getCor()).isEqualTo(Cor.AMBAR);
		assertThat(lida.getAnotacoes().get(0).getId()).isEqualTo("672f0000000000000000c001");
	}

}
