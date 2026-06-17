package br.com.contastermometro.orcamento.service.impl

import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.orcamento.domain.CalculadoraFluxoCaixa
import br.com.contastermometro.orcamento.domain.CalculadoraGastoDiario
import br.com.contastermometro.orcamento.domain.CalculadoraInvestimentos
import br.com.contastermometro.orcamento.dto.ResumoMensal
import br.com.contastermometro.orcamento.service.OrcamentoService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class OrcamentoServiceImpl (
    private val lancamentosService: LancamentosService
) : OrcamentoService {

    override fun gerarResumoMensal(mesRaw: LocalDate): ResumoMensal {
        val yearMonth = YearMonth.from(mesRaw)
        val lancamentos = lancamentosService.listarPorMes(yearMonth)

        val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
        val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)

        val totalInvestido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)
        val porcentagemInvestida = CalculadoraInvestimentos.calcularPorcentagemInvestida(totalInvestido, entradas)
        val metaInvestimento = CalculadoraInvestimentos.buscarMetaDeInvestimento()
        val performance = CalculadoraInvestimentos.calcularPerformanceContraMeta(porcentagemInvestida, metaInvestimento)

        val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)

        val saidaTotal = CalculadoraFluxoCaixa.calcularSaidaTotal(saidasFixas, gastoDiarioTotal)
        val saldo = CalculadoraFluxoCaixa.calcularSaldoDoMes(entradas, saidaTotal)
        val limiteMensalGastoDiario = saldo - totalInvestido

        val gastoDiarioEsperadoAtual = CalculadoraGastoDiario.calcularGastoDiarioEsperadoAteHoje(limiteMensalGastoDiario, mesRaw.dayOfMonth, mesRaw.lengthOfMonth())
        val gastoDiarioRestante = CalculadoraGastoDiario.calcularGastoDiarioRestante(limiteMensalGastoDiario, gastoDiarioTotal)

        return ResumoMensal(
            somaEntradas =  entradas,
            somaSaidasFixas = saidasFixas,
            totalGastoDiario =  gastoDiarioTotal,
            totalInvestido =  totalInvestido,
            saidaTotal =  saidaTotal,
            saldoMes =  saldo,
            porcentagemInvestida =  porcentagemInvestida,
            metaInvestimento =  metaInvestimento,
            performanceContraMeta =  performance,
            gastoDiarioEsperadoAtual =  gastoDiarioEsperadoAtual,
            gastoDiarioRestante =  gastoDiarioRestante
        )
    }
}