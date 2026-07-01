package br.com.contastermometro.consultor.service.impl

import br.com.contastermometro.consultor.dto.MesCriticoResponse
import br.com.contastermometro.consultor.dto.RecomendacaoCompra
import br.com.contastermometro.consultor.dto.SimularCompraRequest
import br.com.contastermometro.consultor.dto.SimularCompraResponse
import br.com.contastermometro.consultor.service.ConsultorService
import br.com.contastermometro.projecao.service.ProjecaoService
import org.springframework.stereotype.Service

@Service
class ConsultorServiceImpl(
    private val projecaoService: ProjecaoService
) : ConsultorService {

    override fun simularCompra(request: SimularCompraRequest): SimularCompraResponse {
        val valorParcela = request.valorTotalCentavos / request.numeroParcelas

        val projecaoAtual = projecaoService.projetarMeses(request.mesInicio, request.numeroParcelas)

        val projecaoComCompra = projecaoAtual.map { p ->
            p.copy(
                parcelamentos = p.parcelamentos + valorParcela,
                totalCompromissos = p.totalCompromissos + valorParcela,
                saldoProjectado = p.saldoProjectado - valorParcela
            )
        }

        val mesesCriticos = projecaoComCompra
            .filter { it.saldoProjectado < 0 || it.totalCompromissos > (it.entradasRecorrentes * 0.85).toLong() }
            .map { p ->
                val atual = projecaoAtual.first { it.mes == p.mes }
                MesCriticoResponse(
                    mes = p.mes,
                    entradasCentavos = p.entradasRecorrentes,
                    comprometimentoAtualCentavos = atual.totalCompromissos,
                    comprometimentoComCompraCentavos = p.totalCompromissos,
                    saldoComCompraCentavos = p.saldoProjectado
                )
            }

        val comprometimentoMedioAtual = projecaoAtual.map { it.totalCompromissos }.average().toLong()
        val comprometimentoMedioCompra = projecaoComCompra.map { it.totalCompromissos }.average().toLong()

        val (recomendacao, motivacao) = when {
            mesesCriticos.any { it.saldoComCompraCentavos < 0 } ->
                RecomendacaoCompra.NAO_RECOMENDADO to "Em ${mesesCriticos.count { it.saldoComCompraCentavos < 0 }} mês(es) o saldo projetado fica negativo com esta compra."
            mesesCriticos.isNotEmpty() ->
                RecomendacaoCompra.ARRISCADO to "A compra compromete mais de 85% da renda recorrente em ${mesesCriticos.size} mês(es). Reserve uma margem de segurança."
            comprometimentoMedioCompra > comprometimentoMedioAtual * 1.30 ->
                RecomendacaoCompra.ARRISCADO to "O comprometimento médio mensal aumenta mais de 30% com esta compra."
            else ->
                RecomendacaoCompra.VIAVEL to "O orçamento recorrente absorve as parcelas sem meses críticos."
        }

        return SimularCompraResponse(
            recomendacao = recomendacao,
            motivacao = motivacao,
            valorParcelaCentavos = valorParcela,
            comprometimentoMedioAtualCentavos = comprometimentoMedioAtual,
            comprometimentoMedioComCompraCentavos = comprometimentoMedioCompra,
            mesesCriticos = mesesCriticos
        )
    }
}