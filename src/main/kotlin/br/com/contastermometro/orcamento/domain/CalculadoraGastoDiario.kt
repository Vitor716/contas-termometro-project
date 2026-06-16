package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import java.math.BigDecimal

object CalculadoraGastoDiario {

    fun calcularTotalGastoDiario(lancamentos: List<LancamentoResponse>): BigDecimal {
        TODO("Implementar soma de lançamentos do tipo GASTO_DIARIO")
    }

    /**
     * Calcula qual deveria ser o limite de gasto até o dia de hoje para não estourar o orçamento do mês.
     * Necessita do limite mensal total disponível para gastos diários e do progresso do mês.
     */
    fun calcularGastoDiarioEsperadoAteHoje(
        limiteMensalGastoDiario: BigDecimal, // O que sobrou das entradas após pagar fixas e investir
        diaAtual: Int,
        totalDiasDoMes: Int
    ): BigDecimal {
        TODO("Implementar: (Limite Mensal / Total de Dias) * Dia Atual")
    }

    /**
     * Calcula quanto de dinheiro ainda resta para queimar com gasto diário no mês.
     */
    fun calcularGastoDiarioRestante(
        limiteMensalGastoDiario: BigDecimal,
        totalGastoDiarioAteHoje: BigDecimal
    ): BigDecimal {
        TODO("Implementar: Limite Mensal - O que já foi gasto")
    }
}