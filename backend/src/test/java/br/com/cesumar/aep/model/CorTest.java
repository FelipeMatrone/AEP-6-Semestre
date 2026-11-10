package br.com.cesumar.aep.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

// Mesmos três caminhos de porSlug que PrioridadeTest cobre: casar na primeira
// iteração, casar na última e esgotar o laço. Só o terceiro lança.
class CorTest {

	@Test
	void deveResolverOSlugQueCasaNaPrimeiraIteracao() {
		assertThat(Cor.porSlug("roxo")).isEqualTo(Cor.ROXO);
	}

	@Test
	void deveResolverOSlugQueCasaNaUltimaIteracao() {
		assertThat(Cor.porSlug("ambar")).isEqualTo(Cor.AMBAR);
	}

	@Test
	void deveLancarExcecaoQuandoOLacoSeEsgotaSemCasar() {
		assertThatThrownBy(() -> Cor.porSlug("azul"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("azul");
	}

	// o validador da coleção categorias declara este mesmo conjunto de slugs no
	// $jsonSchema (enum). Renomear um slug aqui sem mexer no validador faria o
	// insert ser rejeitado em runtime; este teste transforma isso em falha de build.
	@Test
	void deveExporOSlugAsciiDeCadaConstante() {
		assertThat(Cor.ROXO.getSlug()).isEqualTo("roxo");
		assertThat(Cor.MAGENTA.getSlug()).isEqualTo("magenta");
		assertThat(Cor.VERDE.getSlug()).isEqualTo("verde");
		assertThat(Cor.AMBAR.getSlug()).isEqualTo("ambar");
	}

}
