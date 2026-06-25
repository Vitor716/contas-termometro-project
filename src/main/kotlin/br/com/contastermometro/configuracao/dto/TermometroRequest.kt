package br.com.contastermometro.configuracao.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class TermometroRequest(
    @field:NotNull(message = "A reserva mínima intocável é obrigatória")
    @field:PositiveOrZero(message = "A reserva deve ser um valor positivo")
    val reservaMinimaIntocavel: BigDecimal,

    @field:NotNull(message = "O orçamento diário mínimo é obrigatório")
    @field:PositiveOrZero(message = "O orçamento diário deve ser um valor positivo")
    val orcamentoDiarioMinimo: BigDecimal,

    @field:NotNull(message = "O comprometimento máximo da renda é obrigatório")
    val comprometimentoMaximoRenda: BigDecimal,

    @field:NotNull(message = "A margem de segurança é obrigatória")
    val margemSeguranca: BigDecimal,

    @field:NotNull(message = "A estratégia é obrigatória")
    val estrategia: EstrategiaTermometro
)