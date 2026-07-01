package br.com.contastermometro.projecao.service.impl

import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.projecao.dto.ItemProjecaoResponse
import br.com.contastermometro.projecao.dto.ProjecaoMensalResponse
import br.com.contastermometro.projecao.service.ProjecaoService
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class ProjecaoServiceImpl(
    private val recorrenciaRepository: RecorrenciaLancamentoRepository
) : ProjecaoService {

    override fun projetarMeses(mesInicioStr: String, quantidade: Int): List<ProjecaoMensalResponse> {
        val inicio = YearMonth.parse(mesInicioStr)
        return (0 until quantidade).map { i ->
            projetarMes(inicio.plusMonths(i.toLong()))
        }
    }

    private fun projetarMes(ym: YearMonth): ProjecaoMensalResponse {
        val mes = ym.toString()
        val ativas = recorrenciaRepository.findAtivasPorMes(mes, StatusParcelamento.ATIVO)

        val itens = ativas.map { r ->
            val isParc = r.categoria == "PARCELAMENTO" && r.mesFim != null
            val startMes = toMonthInt(r.mesInicio)
            val nowMes = toMonthInt(mes)

            val totalParc = if (isParc) toMonthInt(r.mesFim!!) - startMes + 1 else null
            val atualParc = if (isParc) nowMes - startMes + 1 else null

            ItemProjecaoResponse(
                id = r.id!!,
                descricao = r.descricao,
                valorCentavos = r.valorCentavos,
                tipo = r.tipo,
                categoria = r.categoria,
                isParcelamento = isParc,
                parcelaAtual = atualParc,
                parcelaTotal = totalParc
            )
        }

        val entradas = itens.filter { it.tipo == TipoLancamento.ENTRADA }.sumOf { it.valorCentavos }
        val saidasFixas = itens.filter { it.tipo == TipoLancamento.SAIDA_FIXA }.sumOf { it.valorCentavos }
        val parcelamentos = itens.filter { it.isParcelamento }.sumOf { it.valorCentavos }
        val outrosGastos = itens.filter {
            it.tipo == TipoLancamento.GASTO_DIARIO && !it.isParcelamento
        }.sumOf { it.valorCentavos }

        val totalComprometido = saidasFixas + parcelamentos + outrosGastos

        return ProjecaoMensalResponse(
            mes = mes,
            entradasRecorrentes = entradas,
            saidasFixas = saidasFixas,
            parcelamentos = parcelamentos,
            outrosGastosRecorrentes = outrosGastos,
            totalCompromissos = totalComprometido,
            saldoProjectado = entradas - totalComprometido,
            itens = itens
        )
    }

    private fun toMonthInt(ym: String): Int {
        val (y, m) = ym.split("-").map(String::toInt)
        return y * 12 + m - 1
    }
}