package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.AnteciparParcelamentoRequest
import br.com.contastermometro.lancamentos.dto.CancelarRecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.RecorrenciaLancamento
import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaResponse
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.lancamentos.service.RecorrenciaLancamentoService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class RecorrenciaLancamentoServiceImpl (
    private val recorrenciaRepository: RecorrenciaLancamentoRepository,
    private val lancamentoRepository: LancamentoRepository,
) : RecorrenciaLancamentoService{

    @Transactional
    override fun criar(request: RecorrenciaRequest): RecorrenciaResponse {
        if (request.tipo != TipoLancamento.ENTRADA && request.tipo != TipoLancamento.SAIDA_FIXA && request.tipo != TipoLancamento.GASTO_DIARIO) {
            throw IllegalArgumentException("Apenas ENTRADA e SAIDA_FIXA podem ser recorrentes.")
        }

        val recorrencia = RecorrenciaLancamento(
            tipo = request.tipo,
            descricao = request.descricao,
            valorCentavos = request.valorCentavos,
            categoria = request.categoria,
            observacao = request.observacao,
            idLote = request.idLote,
            mesInicio = request.mesInicio,
            mesFim = request.mesFim,
            diaPreferencial = request.diaPreferencial,
            parcelaInicio = request.parcelaInicio,
            parcelaTotal = request.parcelaTotal,
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

    override fun listarPorMes(mesRaw: YearMonth): List<RecorrenciaResponse> {
        val mes = mesRaw.toString()

        val entities = recorrenciaRepository.findVigentesParaOMes(mes)
        return entities.map { it.toResponse() }
    }

    override fun listarParcelamentos(categoria: String): List<RecorrenciaResponse> {
        val entities = recorrenciaRepository.findByCategoria(categoria)
        return entities.map { it.toResponse() }
    }

    @Transactional
    override fun editar(id: Long, request: RecorrenciaRequest): RecorrenciaResponse {
        val rec = recorrenciaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Recorrencia $id nao encontrada") }

        rec.tipo = request.tipo
        rec.descricao = request.descricao
        rec.valorCentavos = request.valorCentavos
        rec.categoria = request.categoria
        rec.observacao = request.observacao
        rec.idLote = request.idLote
        rec.mesInicio = request.mesInicio
        rec.mesFim = request.mesFim
        rec.diaPreferencial = request.diaPreferencial
        rec.parcelaInicio = request.parcelaInicio
        rec.parcelaTotal = request.parcelaTotal
        rec.frequencia = request.frequencia
        rec.status = request.status

        return recorrenciaRepository.save(rec).toResponse()
    }

    @Transactional
    override fun cancelar(id: Long, request: CancelarRecorrenciaRequest): RecorrenciaResponse {
        val rec = recorrenciaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Recorrência $id não encontrada") }

        val aPartirDe = request.aPartirDe?.let { YearMonth.parse(it) } ?: YearMonth.now().plusMonths(1)
        val ultimoMesAtivo = aPartirDe.minusMonths(1).toString()

        rec.mesFim = if (ultimoMesAtivo < rec.mesInicio) rec.mesInicio else ultimoMesAtivo
        rec.status = StatusParcelamento.INATIVO

        return recorrenciaRepository.save(rec).toResponse()
    }

    @Transactional
    override fun antecipar(id: Long, request: AnteciparParcelamentoRequest): RecorrenciaResponse {
        val rec = recorrenciaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Recorrência $id não encontrada") }

        require(rec.categoria == "PARCELAMENTO") { "Antecipação só é aplicável a parcelamentos" }
        require(rec.mesFim != null) { "Parcelamento sem data de fim não pode ser antecipado" }
        require(request.mesQuitacao <= rec.mesFim!!) {
            "Mês de quitação (${request.mesQuitacao}) é posterior ao término original (${rec.mesFim})"
        }

        val mesPosteriorQuitacao = YearMonth.parse(request.mesQuitacao).plusMonths(1)
        lancamentoRepository.deleteMaterializadosDaRecorrenciaNoIntervalo(
            recorrenciaId = rec.id!!,
            mesInicio = mesPosteriorQuitacao.toString(),
            mesFimExclusivo = YearMonth.parse(rec.mesFim!!).plusMonths(1).toString(),
        )

        rec.mesFim = request.mesQuitacao
        rec.status = StatusParcelamento.INATIVO

        return recorrenciaRepository.save(rec).toResponse()
    }
    @Transactional
    override fun remover(id: Long) {
        val rec = recorrenciaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Recorrencia $id nao encontrada") }
        val recorrenciaId = rec.id ?: id

        lancamentoRepository.deleteByRecorrenciaIdIn(listOf(recorrenciaId))
        recorrenciaRepository.delete(rec)
    }
}
