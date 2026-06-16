package br.com.contastermometro.orcamento.domain

import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import java.math.BigDecimal

object CalculadoraInvestimentos {

    fun calcularTotalInvestido(lancamentos: List<LancamentoResponse>): BigDecimal {
        TODO("Implementar soma de lançamentos do tipo INVESTIMENTO / ECONOMIA")
    }

    fun calcularPorcentagemInvestida(totalInvestido: BigDecimal, somaEntradas: BigDecimal): BigDecimal {
        TODO("Implementar: (Total Investido / Soma Entradas) * 100. Atenção: Tratar divisão por zero caso Entradas sejam 0.")
    }

    fun buscarMetaDeInvestimento(): BigDecimal {
        // No futuro, isso pode vir de uma entidade de configuração do banco de dados.
        // Por enquanto, você pode mockar um valor aqui (ex: 20%).
        TODO("Retornar percentual ou valor da meta de investimento configurada")
    }

    fun calcularPerformanceContraMeta(porcentagemInvestida: BigDecimal, meta: BigDecimal): BigDecimal {
        TODO("Implementar a diferença de performance. (ex: Meta era 20%, investiu 15% -> Performance: -5%)")
    }
}