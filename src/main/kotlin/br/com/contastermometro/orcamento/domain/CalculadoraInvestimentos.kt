package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import java.math.BigDecimal
import java.math.RoundingMode

object CalculadoraInvestimentos {

    fun calcularTotalInvestido(lancamentos: List<LancamentoResponse>): BigDecimal {
        return lancamentos
            .filter { it.tipo == TipoLancamento.INVESTIMENTO }
            .sumOf { it.valor }
    }

    fun calcularPorcentagemInvestida(totalInvestido: BigDecimal, somaEntradas: BigDecimal): BigDecimal {
        val retorno = somaEntradas.compareTo(BigDecimal.ZERO)
        if(retorno == 0) return BigDecimal.ZERO
        return totalInvestido.divide(somaEntradas, 2, RoundingMode.HALF_UP)
    }

    fun calcularPerformanceContraMeta(porcentagemInvestida: BigDecimal, meta: BigDecimal): BigDecimal {
        if (meta.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return porcentagemInvestida.divide(meta, 4, RoundingMode.HALF_UP)
    }
}