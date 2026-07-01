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
    val frequencia: Frequencia,
    val status: StatusParcelamento,
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
        frequencia = this.frequencia,
        status = this.status
    )
}

fun RecorrenciaRequest.toModel(): RecorrenciaLancamento {
    return RecorrenciaLancamento(
        tipo = this.tipo,
        descricao = this.descricao,
        valorCentavos = this.valorCentavos,
        mesInicio = this.mesInicio,
        mesFim = this.mesFim,
        diaPreferencial = this.diaPreferencial,
        frequencia = this.frequencia,
        status = status,
        categoria = this.categoria,
        observacao = this.observacao
    )
}