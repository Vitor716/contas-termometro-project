package br.com.contastermometro.orcamento.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class SnapshotTermometroRequest(
    @field:NotNull(message = "O mês de referência é obrigatório")
    @field:Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "O formato deve ser YYYY-MM")
    val mesReferencia: String,

    @field:NotNull(message = "O status atual é obrigatório")
    val statusAtual: StatusTermometro,

    @field:NotNull(message = "O gasto diário restante não pode ser nulo")
    val gastoDiarioRestanteCentavos: BigDecimal,

    @field:NotNull(message = "O total investido não pode ser nulo")
    @field:PositiveOrZero(message = "O total investido não pode ser negativo")
    val totalInvestidoCentavos: BigDecimal,

    @field:NotNull(message = "A performance em BPS é obrigatória")
    val performanceContraMetaBps: Int
)