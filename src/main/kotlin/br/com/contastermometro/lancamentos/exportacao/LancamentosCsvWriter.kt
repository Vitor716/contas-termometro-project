package br.com.contastermometro.lancamentos.exportacao

import br.com.contastermometro.lancamentos.model.Lancamento
import java.math.BigDecimal

object LancamentosCsvWriter {
    private val cabecalho = listOf(
        "id",
        "id_lote",
        "mes_referencia",
        "data",
        "tipo",
        "descricao",
        "categoria",
        "valor",
        "observacao",
        "recorrencia_id",
        "recorrencia_excecao",
        "status",
        "criado_em",
        "atualizado_em",
    )

    fun escrever(lancamentos: List<Lancamento>): String {
        return buildString {
            appendLine(cabecalho.joinToString(","))
            lancamentos.forEach { lancamento ->
                appendLine(
                    listOf(
                        lancamento.id?.toString().orEmpty(),
                        lancamento.idLote.orEmpty(),
                        lancamento.mesReferencia,
                        lancamento.dataLancamento,
                        lancamento.tipo.name,
                        lancamento.descricao,
                        lancamento.categoria.orEmpty(),
                        BigDecimal(lancamento.valorCentavos).movePointLeft(2).toPlainString(),
                        lancamento.observacao.orEmpty(),
                        lancamento.recorrenciaId?.toString().orEmpty(),
                        lancamento.recorrenciaExcecao.toString(),
                        lancamento.status.name,
                        lancamento.criadoEm,
                        lancamento.atualizadoEm,
                    ).joinToString(",") { escape(it) }
                )
            }
        }
    }

    private fun escape(valor: String): String {
        val precisaEscapar = valor.any { it == '"' || it == ',' || it == '\n' || it == '\r' }
        if (!precisaEscapar) return valor
        return "\"${valor.replace("\"", "\"\"")}\""
    }
}
