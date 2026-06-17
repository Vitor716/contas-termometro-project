package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import java.math.BigDecimal

object CalculadoraInvestimentos {

    fun calcularTotalInvestido(lancamentos: List<LancamentoResponse>): BigDecimal {
        return lancamentos
            .filter { it.tipo == TipoLancamento.INVESTIMENTO }
            .sumOf { it.valor }
    }

    fun calcularPorcentagemInvestida(totalInvestido: BigDecimal, somaEntradas: BigDecimal): BigDecimal {
        return totalInvestido / somaEntradas * BigDecimal(100)
    }

    fun buscarMetaDeInvestimento(): BigDecimal {
        return BigDecimal(20)
    }

    fun calcularPerformanceContraMeta(porcentagemInvestida: BigDecimal, meta: BigDecimal): BigDecimal {
        return porcentagemInvestida - meta
    }
}