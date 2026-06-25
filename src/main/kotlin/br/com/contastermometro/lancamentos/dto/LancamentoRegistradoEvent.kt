package br.com.contastermometro.lancamentos.dto

import java.math.BigDecimal

data class LancamentoRegistradoEvent(
    val id: String,
    val descricao: String,
    val valor: BigDecimal,
    val data: String
)
