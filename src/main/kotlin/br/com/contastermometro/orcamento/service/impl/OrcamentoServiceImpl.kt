package br.com.contastermometro.orcamento.service.impl

import br.com.contastermometro.configuracao.service.MetaMensalService
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.orcamento.domain.CalculadoraFluxoCaixa
import br.com.contastermometro.orcamento.domain.CalculadoraGastoDiario
import br.com.contastermometro.orcamento.domain.CalculadoraInvestimentos
import br.com.contastermometro.orcamento.dto.ResumoAnualMesItem
import br.com.contastermometro.orcamento.dto.ResumoAnualResponse
import br.com.contastermometro.orcamento.dto.ResumoMensal
import br.com.contastermometro.orcamento.service.OrcamentoService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class OrcamentoServiceImpl (
    private val lancamentosService: LancamentosService,
    private val metaMensalService: MetaMensalService
) : OrcamentoService {

    override fun gerarResumoMensal(mesRaw: LocalDate): ResumoMensal {
        val yearMonth = YearMonth.from(mesRaw)
        val lancamentos = lancamentosService.listarPorMes(yearMonth)

        val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
        val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)

        val totalInvestido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)
        val porcentagemInvestida = CalculadoraInvestimentos.calcularPorcentagemInvestida(totalInvestido, entradas)
        val metaInvestimento = metaMensalService.buscar(yearMonth)
        val performance = CalculadoraInvestimentos.calcularPerformanceContraMeta(porcentagemInvestida, metaInvestimento.percentualMetaInvestimento)

        val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)

        var calcularSomaAjustes = CalculadoraFluxoCaixa.calcularSomaAjustes(lancamentos)
        val saidaTotal = CalculadoraFluxoCaixa.calcularSaidaTotal(saidasFixas, gastoDiarioTotal, totalInvestido)
        val saldo = CalculadoraFluxoCaixa.calcularSaldoDoMes(entradas, saidaTotal, calcularSomaAjustes)

        val metaValorReais = entradas.multiply(metaInvestimento.percentualMetaInvestimento)
        val limiteMensalGastoDiario = entradas - saidasFixas - metaValorReais

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
            metaInvestimento =  metaInvestimento.percentualMetaInvestimento,
            performanceContraMeta =  performance,
            gastoDiarioEsperadoAtual =  gastoDiarioEsperadoAtual,
            gastoDiarioRestante =  gastoDiarioRestante
        )
    }

    override fun gerarResumoAnual(ano: Int): ResumoAnualResponse {
        val mesesItems = (1..12).map { mes ->
            val yearMonth = YearMonth.of(ano, mes)
            val lancamentos = lancamentosService.listarPorMes(yearMonth)

            val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
            val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)
            val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)
            val totalInvestido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)

            val saidaTotal = CalculadoraFluxoCaixa.calcularSaidaTotal(saidasFixas, gastoDiarioTotal, totalInvestido)
            val saldo = CalculadoraFluxoCaixa.calcularSaldoDoMes(entradas, saidaTotal, CalculadoraFluxoCaixa.calcularSomaAjustes(lancamentos))
            val porcentagemInvestida = CalculadoraInvestimentos.calcularPorcentagemInvestida(totalInvestido, entradas)

            ResumoAnualMesItem(
                month = yearMonth.toString(),
                entriesCount = lancamentos.size,
                somaEntradas = entradas,
                somaSaidasFixas = saidasFixas,
                totalGastoDiario = gastoDiarioTotal,
                totalInvestido = totalInvestido,
                saidaTotal = saidaTotal,
                saldoMes = saldo,
                porcentagemInvestida = porcentagemInvestida
            )
        }

        return ResumoAnualResponse(
            ano = ano,
            totalEntradas = mesesItems.sumOf { it.somaEntradas },
            saidaTotal = mesesItems.sumOf { it.saidaTotal },
            totalInvestido = mesesItems.sumOf { it.totalInvestido },
            saldoAcumulado = mesesItems.sumOf { it.saldoMes },
            meses = mesesItems
        )
    }
}