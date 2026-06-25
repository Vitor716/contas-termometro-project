package br.com.contastermometro.orcamento.service.impl

import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import br.com.contastermometro.configuracao.dto.TermometroResponse
import br.com.contastermometro.configuracao.service.MetaMensalService
import br.com.contastermometro.configuracao.service.TermometroService
import br.com.contastermometro.lancamentos.dto.LancamentoRegistradoEvent
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.orcamento.domain.CalculadoraFluxoCaixa
import br.com.contastermometro.orcamento.domain.CalculadoraGastoDiario
import br.com.contastermometro.orcamento.domain.CalculadoraInvestimentos
import br.com.contastermometro.orcamento.dto.SnapshotTermometroMensal
import br.com.contastermometro.orcamento.dto.StatusTermometro
import br.com.contastermometro.orcamento.repository.SnapshotTermometroRepository
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Service
class AnalisadorTermometroListenerImpl(
    private val lancamentoService: LancamentosService,
    private val termometroService: TermometroService,
    private val metaMensalService: MetaMensalService,
    private val snapshotTermometroRepository: SnapshotTermometroRepository
) {
    @EventListener
    @Transactional
    fun handleUserRegistration(event: LancamentoRegistradoEvent) {
        val dataLancamento = LocalDate.parse(event.data)
        val yearMonth = YearMonth.from(dataLancamento)
        val mesReferencia = yearMonth.toString()

        val lancamentos = lancamentoService.listarPorMes(yearMonth)
        val meta = metaMensalService.buscar(yearMonth)
        val configuracao = termometroService.buscar()

        val saidasFixas = CalculadoraFluxoCaixa.calcularSomaSaidasFixas(lancamentos)
        val gastoDiarioTotal = CalculadoraGastoDiario.calcularTotalGastoDiario(lancamentos)
        val totalInvestido = CalculadoraInvestimentos.calcularTotalInvestido(lancamentos)

        val entradas = CalculadoraFluxoCaixa.calcularSomaEntradas(lancamentos)
        val saidaTotal = CalculadoraFluxoCaixa.calcularSaidaTotal(saidasFixas, gastoDiarioTotal, totalInvestido)

        val saldoDisponivel = entradas.subtract(saidaTotal)
        val diasRestantes = calcularDiasRestantes(dataLancamento, yearMonth)

        val limiteDiarioRestante = if (diasRestantes > 0) {
            saldoDisponivel.divide(BigDecimal(diasRestantes), 2, RoundingMode.HALF_UP)
        } else {
            saldoDisponivel
        }

        val status = classificarStatusTermometro(limiteDiarioRestante, totalInvestido, meta, configuracao)

        val performanceBps = calcularPerformanceBps(totalInvestido, meta.percentualMetaInvestimento)

        val snapshot = SnapshotTermometroMensal(
            mesReferencia = mesReferencia,
            statusAtual = status,
            gastoDiarioRestanteCentavos = limiteDiarioRestante,
            totalInvestidoCentavos = totalInvestido,
            performanceContraMetaBps = performanceBps
        )

        snapshotTermometroRepository.save(snapshot)
    }

    private fun calcularDiasRestantes(dataAtual: LocalDate, yearMonth: YearMonth): Int {
        val totalDiasNoMes = yearMonth.lengthOfMonth()
        val diaAtual = dataAtual.dayOfMonth

        return totalDiasNoMes - diaAtual + 1
    }

    private fun classificarStatusTermometro(
        limiteDiarioRestante: BigDecimal,
        totalInvestido: BigDecimal,
        meta: MetaMensalResponse,
        configuracao: TermometroResponse
    ): StatusTermometro {
        if (limiteDiarioRestante < configuracao.orcamentoDiarioMinimo) {
            return StatusTermometro.VERMELHO
        }

        val valorMetaInvestimento = meta.percentualMetaInvestimento
        if (totalInvestido < valorMetaInvestimento) {
            return StatusTermometro.AMARELO
        }

        return StatusTermometro.VERDE
    }

    private fun calcularPerformanceBps(investido: BigDecimal, metaInvestimento: BigDecimal): Int {
        if (metaInvestimento <= BigDecimal.ZERO) return 0

        val percentualAtingido = investido.divide(metaInvestimento, 4, RoundingMode.HALF_UP)
        return percentualAtingido.multiply(BigDecimal(10000)).toInt()
    }
}