package br.com.contastermometro.configuracao.dto

import br.com.contastermometro.configuracao.repository.entity.TermometroEntity
import java.math.BigDecimal

data class TermometroResponse(
    val id: Long,
    val reservaMinimaIntocavel: BigDecimal,
    val orcamentoDiarioMinimo: BigDecimal,
    val comprometimentoMaximoRenda: BigDecimal,
    val margemSeguranca: BigDecimal,
    val estrategia: EstrategiaTermometro
)

fun TermometroEntity.toResponse() = TermometroResponse(
    id = id ?: 0,
    reservaMinimaIntocavel = reservaMinimaIntocavel,
    orcamentoDiarioMinimo = orcamentoDiarioMinimo,
    comprometimentoMaximoRenda = comprometimentoMaximoRenda,
    margemSeguranca = margemSeguranca,
    estrategia = estrategia
)

fun TermometroRequest.toModel() : TermometroEntity {
    return TermometroEntity(
        reservaMinimaIntocavel = this.reservaMinimaIntocavel,
        orcamentoDiarioMinimo = this.orcamentoDiarioMinimo,
        comprometimentoMaximoRenda = this.comprometimentoMaximoRenda,
        margemSeguranca = this.margemSeguranca,
        estrategia = this.estrategia
    )
}