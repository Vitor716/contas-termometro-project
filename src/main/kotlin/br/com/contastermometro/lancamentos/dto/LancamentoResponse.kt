package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.TipoLancamento
import java.math.BigDecimal
import java.time.LocalDate

data class LancamentoResponse(
    var id: Long,
    var tipo: TipoLancamento,
    var descricao: String,
    var valor: BigDecimal,
    var data: LocalDate,
    var mesReferencia: String,
    var categoria: String? = null,
    var observacao: String? = null,
)

