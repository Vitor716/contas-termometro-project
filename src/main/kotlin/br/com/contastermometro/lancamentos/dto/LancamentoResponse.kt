package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import java.math.BigDecimal
import java.time.LocalDate

data class LancamentoResponse(
    var id: Long,
    var idLote: String? = null,
    var tipo: TipoLancamento,
    var descricao: String,
    var valor: BigDecimal,
    var data: LocalDate,
    var mesReferencia: String,
    var categoria: String? = null,
    var observacao: String? = null,
    var recorrenciaId: Long? = null,
    var recorrenciaExcecao: Boolean = false,
)

fun Lancamento.toResponse(): LancamentoResponse {
    return LancamentoResponse(
        id = id ?: 0,
        idLote = this.idLote,
        tipo = tipo,
        descricao = descricao,
        valor = BigDecimal(valorCentavos).movePointLeft(2),
        data = LocalDate.parse(dataLancamento),
        mesReferencia = mesReferencia,
        categoria = categoria,
        observacao = observacao,
        recorrenciaId = recorrenciaId,
        recorrenciaExcecao = recorrenciaExcecao,
    )
}

fun LancamentoRequest.toModel(): Lancamento {
    return Lancamento(
        tipo = this.tipo,
        idLote = this.idLote,
        descricao = this.descricao,
        valorCentavos = this.valor.movePointRight(2).toLong(),
        dataLancamento = this.data.toString(),
        mesReferencia = mesReferencia,
        categoria = categoria,
        observacao = observacao,
        recorrenciaId      = recorrenciaId,
        recorrenciaExcecao = recorrenciaExcecao,
    )
}

