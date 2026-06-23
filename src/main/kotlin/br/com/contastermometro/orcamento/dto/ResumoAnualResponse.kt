package br.com.contastermometro.orcamento.dto

import java.math.BigDecimal

data class ResumoAnualResponse(
    val ano: Int,
    val totalEntradas: BigDecimal,
    val saidaTotal: BigDecimal,
    val totalInvestido: BigDecimal,
    val saldoAcumulado: BigDecimal,
    val meses: List<ResumoAnualMesItem>
)