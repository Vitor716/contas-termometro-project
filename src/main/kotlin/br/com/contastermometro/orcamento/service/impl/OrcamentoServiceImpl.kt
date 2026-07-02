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
import br.com.contastermometro.projecao.service.ProjecaoService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Service
class OrcamentoServiceImpl (
    private val lancamentosService: LancamentosService,
    private val metaMensalService: MetaMensalService,
    private val projecaoService: ProjecaoService
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
        val projecoesPorMes = projecaoService
            .projetarMeses(YearMonth.of(ano, 1).toString(), 12)
            .associateBy { it.mes }

        val mesesItems = (1..12).map { mes ->
            val yearMonth = YearMonth.of(ano, mes)
            val lancamentos = lancamentosService.listarPorMes(yearMonth)
            val metaInvestimento = metaMensalService.buscar(yearMonth)
            val projecao = projecoesPorMes[yearMonth.toString()]

            val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
            val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)
            val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)
            val totalInvestido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)

            val saidaTotal = CalculadoraFluxoCaixa.calcularSaidaTotal(saidasFixas, gastoDiarioTotal, totalInvestido)
            val saldo = CalculadoraFluxoCaixa.calcularSaldoDoMes(entradas, saidaTotal, CalculadoraFluxoCaixa.calcularSomaAjustes(lancamentos))
            val porcentagemInvestida = CalculadoraInvestimentos.calcularPorcentagemInvestida(totalInvestido, entradas)
            val performance = CalculadoraInvestimentos.calcularPerformanceContraMeta(
                porcentagemInvestida,
                metaInvestimento.percentualMetaInvestimento
            )
            val entradasRecorrentes = projecao?.entradasRecorrentes ?: 0
            val totalCompromissos = projecao?.totalCompromissos ?: 0
            val taxaComprometimento = calcularTaxaComprometimento(totalCompromissos, entradasRecorrentes)

            ResumoAnualMesItem(
                month = yearMonth.toString(),
                entriesCount = lancamentos.size,
                somaEntradas = entradas,
                somaSaidasFixas = saidasFixas,
                totalGastoDiario = gastoDiarioTotal,
                totalInvestido = totalInvestido,
                saidaTotal = saidaTotal,
                saldoMes = saldo,
                porcentagemInvestida = porcentagemInvestida,
                metaInvestimento = metaInvestimento.percentualMetaInvestimento,
                performanceContraMeta = performance,
                parcelamentos = centavosParaReais(projecao?.parcelamentos ?: 0),
                totalCompromissosProjetados = centavosParaReais(totalCompromissos),
                saldoProjetado = centavosParaReais(projecao?.saldoProjectado ?: 0),
                taxaComprometimento = taxaComprometimento
            )
        }

        val totalEntradas = mesesItems.sumOf { it.somaEntradas }
        val saidaTotal = mesesItems.sumOf { it.saidaTotal }
        val totalInvestido = mesesItems.sumOf { it.totalInvestido }
        val saldoAcumulado = mesesItems.sumOf { it.saldoMes }
        val mesesAtivos = mesesItems.filter { it.entriesCount > 0 }
        val divisorAtivo = mesesAtivos.size.takeIf { it > 0 } ?: 12
        val mesesRanking = mesesItems.filter { it.entriesCount > 0 || it.somaEntradas > BigDecimal.ZERO }

        return ResumoAnualResponse(
            ano = ano,
            totalEntradas = totalEntradas,
            saidaTotal = saidaTotal,
            totalInvestido = totalInvestido,
            saldoAcumulado = saldoAcumulado,
            percentualInvestidoAnual = CalculadoraInvestimentos.calcularPorcentagemInvestida(totalInvestido, totalEntradas),
            mediaMensalEntradasCalendario = dividir(totalEntradas, 12),
            mediaMensalEntradasAtiva = dividir(totalEntradas, divisorAtivo),
            mediaMensalSaidasCalendario = dividir(saidaTotal, 12),
            mediaMensalSaidasAtiva = dividir(saidaTotal, divisorAtivo),
            mediaMensalInvestidaCalendario = dividir(totalInvestido, 12),
            mediaMensalInvestidaAtiva = dividir(totalInvestido, divisorAtivo),
            mesesComMovimentacao = mesesAtivos.size,
            maiorEntrada = mesesRanking.maxByOrNull { it.somaEntradas },
            maiorInvestimento = mesesRanking.maxByOrNull { it.totalInvestido },
            maiorSaida = mesesRanking.maxByOrNull { it.saidaTotal },
            melhorMes = mesesRanking.maxWithOrNull(rankingMesComparator),
            piorMes = mesesRanking.minWithOrNull(rankingMesComparator),
            totalParcelamentos = mesesItems.sumOf { it.parcelamentos },
            totalCompromissosProjetados = mesesItems.sumOf { it.totalCompromissosProjetados },
            mediaComprometimento = dividir(mesesItems.sumOf { it.taxaComprometimento }, 12),
            meses = mesesItems
        )
    }

    private val rankingMesComparator = compareBy<ResumoAnualMesItem> { it.performanceContraMeta }
        .thenBy { it.saldoMes }
        .thenBy { it.porcentagemInvestida }

    private fun centavosParaReais(valorCentavos: Long): BigDecimal {
        return BigDecimal.valueOf(valorCentavos, 2)
    }

    private fun calcularTaxaComprometimento(totalCompromissos: Long, entradasRecorrentes: Long): BigDecimal {
        if (entradasRecorrentes == 0L) return BigDecimal.ZERO
        return BigDecimal.valueOf(totalCompromissos)
            .divide(BigDecimal.valueOf(entradasRecorrentes), 4, RoundingMode.HALF_UP)
    }

    private fun dividir(valor: BigDecimal, divisor: Int): BigDecimal {
        return valor.divide(BigDecimal.valueOf(divisor.toLong()), 2, RoundingMode.HALF_UP)
    }
}
