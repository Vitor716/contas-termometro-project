package br.com.contastermometro.importacao.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NubankCsvParserTest {

    private val parser = NubankCsvParser()

    @Test
    fun `detecta parcelamento escrito como parcela no csv do nubank`() {
        val csv = """
            date,title,amount
            2026-06-27,Vans*Order11099643 - Parcela 2/3,"143,79"
        """.trimIndent()

        val linhas = parser.extrair(csv)

        assertThat(linhas).hasSize(1)
        assertThat(linhas[0].descricaoLimpa).isEqualTo("Vans*Order11099643")
        assertThat(linhas[0].categoriaSugerida).isEqualTo("PARCELAMENTO")
        assertThat(linhas[0].isParcelamento).isTrue()
        assertThat(linhas[0].parcelaAtual).isEqualTo(2)
        assertThat(linhas[0].parcelaTotal).isEqualTo(3)
        assertThat(linhas[0].valorCentavos).isEqualTo(14379)
    }
}
