package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.Frequencia
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento

data class RecorrenciaResponse(
    val id: Long,
    val tipo: TipoLancamento,
    val descricao: String,
    val valorCentavos: Long,
    val mesInicio: String,
    val mesFim: String?,
    val diaPreferencial: Int,
    val parcelaInicio: Int? = null,
    val parcelaTotal: Int? = null,
    val frequencia: Frequencia,
    val status: StatusParcelamento,
    val categoria: String? = null,
    val observacao: String? = null,
    val idLote: String? = null,
)


fun RecorrenciaLancamento.toResponse(): RecorrenciaResponse {
    return RecorrenciaResponse(
        id = id!!,
        tipo = this.tipo,
        descricao = this.descricao,
        valorCentavos = this.valorCentavos,
        mesInicio = this.mesInicio,
        mesFim = this.mesFim,
        diaPreferencial = this.diaPreferencial,
        parcelaInicio = this.parcelaInicio ?: inferirProximaParcela(this.observacao),
        parcelaTotal = this.parcelaTotal ?: inferirTotalParcelas(this.observacao),
        frequencia = this.frequencia,
        status = this.status,
        categoria = this.categoria,
        observacao = this.observacao,
        idLote = this.idLote
    )
}

private fun inferirProximaParcela(observacao: String?): Int? {
    val match = Regex("""parcelas?\s+(\d+)(?:-(\d+))?/(\d+)""", RegexOption.IGNORE_CASE)
        .find(observacao ?: "")
        ?: return null
    return (match.groupValues[2].toIntOrNull() ?: match.groupValues[1].toIntOrNull())?.plus(1)
}

private fun inferirTotalParcelas(observacao: String?): Int? {
    val match = Regex("""parcelas?\s+\d+(?:-\d+)?/(\d+)""", RegexOption.IGNORE_CASE)
        .find(observacao ?: "")
        ?: return null
    return match.groupValues[1].toIntOrNull()
}

fun RecorrenciaRequest.toModel(): RecorrenciaLancamento {
    return RecorrenciaLancamento(
        tipo = this.tipo,
        descricao = this.descricao,
        valorCentavos = this.valorCentavos,
        mesInicio = this.mesInicio,
        mesFim = this.mesFim,
        diaPreferencial = this.diaPreferencial,
        parcelaInicio = this.parcelaInicio,
        parcelaTotal = this.parcelaTotal,
        frequencia = this.frequencia,
        status = status,
        categoria = this.categoria,
        observacao = this.observacao,
        idLote = this.idLote
    )
}
