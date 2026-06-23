package br.com.contastermometro.configuracao.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class MetaMensalRequest(
    @field:NotNull(message = "O percentual da meta não pode ser nulo.")
    @field:DecimalMin(value = "0.0", message = "O percentual mínimo é 0.0 (0%)")
    @field:DecimalMax(value = "1.0", message = "O percentual máximo é 1.0 (100%)")
    val percentualMetaInvestimento: BigDecimal,

    @field:NotNull(message = "O orçamento diário mínimo não pode ser nulo.")
    @field:PositiveOrZero(message = "O orçamento diário não pode ser negativo.")
    val orcamentoDiarioMinimo: BigDecimal,

    val motivo: String? = null
)