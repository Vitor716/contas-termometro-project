package br.com.contastermometro.orcamento.dto

import java.math.BigDecimal

data class ResumoMensal(
    val somaEntradas: BigDecimal,
    val somaSaidasFixas: BigDecimal,
    val totalGastoDiario: BigDecimal,
    val totalInvestido: BigDecimal,
    val saidaTotal: BigDecimal,
    val saldoMes: BigDecimal,
    val porcentagemInvestida: BigDecimal,
    val metaInvestimento: BigDecimal,
    val performanceContraMeta: BigDecimal,
    val gastoDiarioEsperadoAtual: BigDecimal,
    val gastoDiarioRestante: BigDecimal
)