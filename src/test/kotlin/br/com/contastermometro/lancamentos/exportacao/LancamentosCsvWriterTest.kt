package br.com.contastermometro.lancamentos.exportacao

import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LancamentosCsvWriterTest {
    @Test
    fun `deve escapar campos com virgula aspas e quebra de linha`() {
        val csv = LancamentosCsvWriter.escrever(
            listOf(
                Lancamento(
                    id = 10,
                    idLote = "lote-1",
                    tipo = TipoLancamento.GASTO_DIARIO,
                    descricao = "Mercado, padaria e \"extras\"",
                    valorCentavos = 12345,
                    dataLancamento = "2026-07-10",
                    mesReferencia = "2026-07",
                    categoria = "Alimentacao",
                    observacao = "linha 1\nlinha 2",
                    recorrenciaId = 99,
                    recorrenciaExcecao = true,
                    criadoEm = "2026-07-10T10:00:00Z",
                    atualizadoEm = "2026-07-11T10:00:00Z",
                )
            )
        )

        val esperado = listOf(
            "id,id_lote,mes_referencia,data,tipo,descricao,categoria,valor,observacao,recorrencia_id,recorrencia_excecao,status,criado_em,atualizado_em",
            "10,lote-1,2026-07,2026-07-10,GASTO_DIARIO,\"Mercado, padaria e \"\"extras\"\"\",Alimentacao,123.45,\"linha 1",
            "linha 2\",99,true,ATIVO,2026-07-10T10:00:00Z,2026-07-11T10:00:00Z",
        ).joinToString("\n") + "\n"

        assertEquals(esperado, csv)
    }
}
