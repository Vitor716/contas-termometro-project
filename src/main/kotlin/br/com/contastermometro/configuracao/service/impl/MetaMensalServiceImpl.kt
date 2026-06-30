package br.com.contastermometro.configuracao.service.impl

import br.com.contastermometro.configuracao.dto.MetaMensalRequest
import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import br.com.contastermometro.configuracao.dto.toModel
import br.com.contastermometro.configuracao.dto.toResponse
import br.com.contastermometro.configuracao.repository.MetaMensalRepository
import br.com.contastermometro.configuracao.service.MetaMensalService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class MetaMensalServiceImpl (
    private val repository: MetaMensalRepository
) : MetaMensalService {

    override fun criar(
        mesRaw: YearMonth,
        request: MetaMensalRequest
    ): MetaMensalResponse {
        val entity = request.toModel(mesRaw)
        val saved = repository.save(entity)
        return saved.toResponse()
    }

    override fun buscar(mesRaw: YearMonth): MetaMensalResponse {
        val mes = mesRaw.toString()
        val entity = repository.findByMesReferencia(mes)

        return entity?.toResponse() ?: MetaMensalResponse(
            mesReferencia = mes,
            percentualMetaInvestimento = BigDecimal("0.20"),
            orcamentoDiarioMinimo = BigDecimal.ZERO,
            vigenteDesde = LocalDateTime.now().toString()
        )
    }

    override fun editar(id: Long, request: MetaMensalRequest): MetaMensalResponse {
        val updatedEntity = repository.findById(id).orElseThrow { IllegalArgumentException("Meta mensal com id $id não encontrada.") }.apply {
            percentualMetaInvestimentoBps = request.percentualMetaInvestimento.movePointRight(4).toInt()
            orcamentoDiarioMinimoCentavos = request.orcamentoDiarioMinimo.movePointRight(2).toLong()
            motivo = request.motivo
        }

        val saved = repository.save(updatedEntity)
        return saved.toResponse()
    }
}