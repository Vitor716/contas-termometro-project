package br.com.contastermometro.orcamento.dto

import java.math.BigDecimal

data class ResumoAnualMesItem(
    val month: String,
    val entriesCount: Int,
    val somaEntradas: BigDecimal,
    val somaSaidasFixas: BigDecimal,
    val totalGastoDiario: BigDecimal,
    val totalInvestido: BigDecimal,
    val saidaTotal: BigDecimal,
    val saldoMes: BigDecimal,
    val porcentagemInvestida: BigDecimal
)