package br.com.contastermometro.orcamento.dto

import java.math.BigDecimal

data class ResumoAnualResponse(
    val ano: Int,
    val totalEntradas: BigDecimal,
    val saidaTotal: BigDecimal,
    val totalInvestido: BigDecimal,
    val saldoAcumulado: BigDecimal,
    val percentualInvestidoAnual: BigDecimal,
    val mediaMensalEntradasCalendario: BigDecimal,
    val mediaMensalEntradasAtiva: BigDecimal,
    val mediaMensalSaidasCalendario: BigDecimal,
    val mediaMensalSaidasAtiva: BigDecimal,
    val mediaMensalInvestidaCalendario: BigDecimal,
    val mediaMensalInvestidaAtiva: BigDecimal,
    val mesesComMovimentacao: Int,
    val maiorEntrada: ResumoAnualMesItem?,
    val maiorInvestimento: ResumoAnualMesItem?,
    val maiorSaida: ResumoAnualMesItem?,
    val melhorMes: ResumoAnualMesItem?,
    val piorMes: ResumoAnualMesItem?,
    val totalParcelamentos: BigDecimal,
    val totalCompromissosProjetados: BigDecimal,
    val mediaComprometimento: BigDecimal,
    val meses: List<ResumoAnualMesItem>
)
