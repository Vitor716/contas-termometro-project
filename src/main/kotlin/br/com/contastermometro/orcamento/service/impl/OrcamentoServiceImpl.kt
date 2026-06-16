package br.com.contastermometro.orcamento.service.impl

import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.orcamento.domain.CalculadoraFluxoCaixa
import br.com.contastermometro.orcamento.domain.CalculadoraGastoDiario
import br.com.contastermometro.orcamento.domain.CalculadoraInvestimentos
import br.com.contastermometro.orcamento.dto.ResumoMensal
import br.com.contastermometro.orcamento.service.OrcamentoService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth

@Service
class OrcamentoServiceImpl (
    private val lancamentosService: LancamentosService
) : OrcamentoService {

    override fun gerarResumoMensal(mesRaw: YearMonth): ResumoMensal {
        val lancamentos = lancamentosService.listarPorMes(mesRaw)

        val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
        val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)
//        val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)
//        val investido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)
//
//        val saidaTotal = saidasFixas + gastoDiarioTotal
//        val saldo = CalculadoraFluxoCaixa.calcularSaldoDoMes(entradas, saidaTotal)
//
//        val performance = CalculadoraInvestimentos.calcularPerformanceContraMeta(entradas, investido)

        return ResumoMensal(
            somaEntradas =  entradas,
            somaSaidasFixas = saidasFixas,
            totalGastoDiario =  BigDecimal.ZERO,
            totalInvestido =  BigDecimal.ZERO,
            saidaTotal =  BigDecimal.ZERO,
            saldoMes =  BigDecimal.ZERO,
            porcentagemInvestida =  BigDecimal.ZERO,
            metaInvestimento =  BigDecimal.ZERO,
            performanceContraMeta =  BigDecimal.ZERO,
            gastoDiarioEsperadoAtual =  BigDecimal.ZERO,
            gastoDiarioRestante =  BigDecimal.ZERO
        )
    }
}