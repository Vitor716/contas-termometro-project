package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import java.math.BigDecimal

object CalculadoraFluxoCaixa {

    fun calcularSomaEntradas(lancamentos: List<LancamentoResponse>): BigDecimal {
        return lancamentos
            .filter { it.tipo == TipoLancamento.ENTRADA }
            .sumOf { it.valor }
    }

    fun calcularSomaSaidasFixas(lancamentos: List<LancamentoResponse>): BigDecimal {
        return lancamentos
            .filter { it.tipo == TipoLancamento.SAIDA_FIXA }
            .sumOf { it.valor }
    }

    fun calcularSaidaTotal(saidasFixas: BigDecimal, gastoDiarioTotal: BigDecimal): BigDecimal {
        TODO("Implementar soma total de saídas")
    }

    fun calcularSaldoDoMes(somaEntradas: BigDecimal, saidaTotal: BigDecimal): BigDecimal {
        TODO("Implementar cálculo do saldo final (Entradas - Saídas Totais)")
    }
}
