package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.TipoLancamento
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class LancamentoRequest(
    @field:NotNull
    val tipo: TipoLancamento,

    @field:NotBlank
    val descricao: String,

    @field:NotNull
    @field:Positive
    val valor: BigDecimal,

    @field:NotNull
    val data: LocalDate,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d{4}-\\d{2}$")
    val mesReferencia: String,

    val categoria: String? = null,

    val observacao: String? = null,
)