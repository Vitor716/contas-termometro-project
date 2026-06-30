package br.com.contastermometro.configuracao.dto

import br.com.contastermometro.configuracao.repository.entity.MetaMensalEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth

data class MetaMensalResponse(
    val id: Long? = null,
    val mesReferencia: String,
    val percentualMetaInvestimento: BigDecimal = BigDecimal("0.20"),
    val orcamentoDiarioMinimo: BigDecimal = BigDecimal.ZERO,
    val motivo: String? = "Configuração Padrão",
    val vigenteDesde: String = LocalDateTime.now().toString(),
    val vigenteAte: String? = null
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