package br.com.contastermometro.configuracao.service.impl

import br.com.contastermometro.configuracao.dto.MetaMensalRequest
import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import br.com.contastermometro.configuracao.dto.toModel
import br.com.contastermometro.configuracao.dto.toResponse
import br.com.contastermometro.configuracao.repository.MetaMensalRepository
import br.com.contastermometro.configuracao.service.MetaMensalService
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class MetaMensalServiceImpl (
    private val repository: MetaMensalRepository
) : MetaMensalService {

    override fun definir(
        mesRaw: YearMonth,
        request: MetaMensalRequest
    ): MetaMensalResponse {
        val entity = request.toModel(mesRaw)
        val saved = repository.save(entity)
        return saved.toResponse()
    }

    override fun buscar(mesRaw: YearMonth): MetaMensalResponse {
        val mes = mesRaw.toString()
        val entity = repository.findByMesReferencia(mes) ?: throw IllegalArgumentException("Meta mensal para o mês $mes não encontrada.")
        return entity.toResponse()
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