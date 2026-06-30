package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.RecorrenciaLancamento
import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaResponse
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.lancamentos.service.RecorrenciaLancamentoService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RecorrenciaLancamentoServiceImpl (
    private val recorrenciaRepository: RecorrenciaLancamentoRepository,
) : RecorrenciaLancamentoService{

    @Transactional
    override fun criar(request: RecorrenciaRequest): RecorrenciaResponse {
        if (request.tipo != TipoLancamento.ENTRADA && request.tipo != TipoLancamento.SAIDA_FIXA) {
            throw IllegalArgumentException("Apenas ENTRADA e SAIDA_FIXA podem ser recorrentes.")
        }

        val recorrencia = RecorrenciaLancamento(
            tipo = request.tipo,
            descricao = request.descricao,
            valorCentavos = request.valorCentavos,
            categoria = request.categoria,
            observacao = request.observacao,
            mesInicio = request.mesInicio,
            mesFim = request.mesFim,
            diaPreferencial = request.diaPreferencial,
            frequencia = request.frequencia,
            status = request.status
        )

        val saved = recorrenciaRepository.save(recorrencia)
        return saved.toResponse()
    }

    override fun buscar(): List<RecorrenciaResponse> {
        val entities = recorrenciaRepository.findAll()
        return entities.map { it.toResponse() }
    }
}