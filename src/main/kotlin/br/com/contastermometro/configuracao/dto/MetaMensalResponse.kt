package br.com.contastermometro.configuracao.dto

import br.com.contastermometro.configuracao.repository.entity.MetaMensalEntity
import java.math.BigDecimal
import java.time.YearMonth

data class MetaMensalResponse(
    val id: Long,
    val mesReferencia: String,
    val percentualMetaInvestimento: BigDecimal,
    val orcamentoDiarioMinimo: BigDecimal,
    val motivo: String?,
    val vigenteDesde: String,
    val vigenteAte: String?
)

fun MetaMensalEntity.toResponse(): MetaMensalResponse {
    return MetaMensalResponse(
        id = this.id ?: 0,
        mesReferencia = this.mesReferencia,
        percentualMetaInvestimento = BigDecimal(this.percentualMetaInvestimentoBps).movePointLeft(4),
        orcamentoDiarioMinimo = BigDecimal(this.orcamentoDiarioMinimoCentavos).movePointLeft(2),
        motivo = this.motivo,
        vigenteDesde = this.vigenteDesde,
        vigenteAte = this.vigenteAte
    )
}

fun MetaMensalRequest.toModel(mes: YearMonth): MetaMensalEntity {
    return MetaMensalEntity(
        mesReferencia = mes.toString(),
        percentualMetaInvestimentoBps = this.percentualMetaInvestimento.movePointRight(4).toInt(),
        orcamentoDiarioMinimoCentavos = this.orcamentoDiarioMinimo.movePointRight(2).toLong(),
        motivo = this.motivo
    )
}