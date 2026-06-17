package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import java.math.BigDecimal

object CalculadoraGastoDiario {

    fun calcularTotalGastoDiario(lancamentos: List<LancamentoResponse>): BigDecimal {
        return lancamentos
            .filter { it.tipo == TipoLancamento.GASTO_DIARIO }
            .sumOf { it.valor }
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
        return limiteMensalGastoDiario / totalDiasDoMes.toBigDecimal()
    }

    /**
     * Calcula quanto de dinheiro ainda resta para queimar com gasto diário no mês.
     */
    fun calcularGastoDiarioRestante(
        limiteMensalGastoDiario: BigDecimal,
        totalGastoDiarioAteHoje: BigDecimal
    ): BigDecimal {
        return limiteMensalGastoDiario - totalGastoDiarioAteHoje
    }
}