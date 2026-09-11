package br.com.cesumar.aep.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

// Casos derivados do grafo de fluxo de porSlug, que é o único método do projeto
// com laço e decisão: os três caminhos possíveis são casar na primeira iteração,
// casar na última e esgotar o laço sem casar. Edge coverage se satisfaz com
// qualquer um dos dois primeiros, mas os três são caminhos distintos do grafo e
// só o terceiro lança.
//
// Antes deste arquivo, "alta" e "baixa" só passavam por porSlug no teste de
// integração, que exige Docker. A restrição de projeto é que a suíte sem Docker
// se sustente sozinha, então esses caminhos precisavam existir aqui.
class PrioridadeTest {

	@Test
	void deveResolverOSlugQueCasaNaPrimeiraIteracao() {
		assertThat(Prioridade.porSlug("alta")).isEqualTo(Prioridade.ALTA);
	}

	@Test
	void deveResolverOSlugQueCasaNaUltimaIteracao() {
		assertThat(Prioridade.porSlug("baixa")).isEqualTo(Prioridade.BAIXA);
	}

	@Test
	void deveLancarExcecaoQuandoOLacoSeEsgotaSemCasar() {
		assertThatThrownBy(() -> Prioridade.porSlug("urgentissima"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("urgentissima");
	}

	// o slug é o contrato com o banco: é exatamente esta lista que o enum do
	// $jsonSchema da coleção valida. Renomear um slug aqui quebra a escrita em
	// produção, e este teste é o que transforma isso em falha de build.
	@Test
	void deveExporOSlugAsciiDeCadaConstante() {
		assertThat(Prioridade.ALTA.getSlug()).isEqualTo("alta");
		assertThat(Prioridade.MEDIA.getSlug()).isEqualTo("media");
		assertThat(Prioridade.BAIXA.getSlug()).isEqualTo("baixa");
	}

}
