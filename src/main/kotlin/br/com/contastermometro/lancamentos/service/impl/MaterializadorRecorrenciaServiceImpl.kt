package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.RecorrenciaLancamento
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.lancamentos.service.MaterializadorRecorrenciaService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class MaterializadorRecorrenciaServiceImpl (
    private val recorrenciaRepository: RecorrenciaLancamentoRepository,
    private val lancamentoRepository: LancamentoRepository,
) : MaterializadorRecorrenciaService {

    @Transactional
    override fun garantirRecorrenciasMaterializadasNoMes(mesReferencia: String) {
        val recorrenciasVigentes = recorrenciaRepository.findVigentesParaOMes(mesReferencia)

        recorrenciasVigentes.forEach { recorrencia ->
            val jaMaterializado = lancamentoRepository.existsByRecorrenciaIdAndMesReferencia(
                recorrencia.id!!,
                mesReferencia
            )

            if (!jaMaterializado) {
                val dataLancamentoString = montarData(mesReferencia, recorrencia.diaPreferencial)

                val novoLancamento = Lancamento(
                    tipo = recorrencia.tipo,
                    descricao = recorrencia.descricao,
                    valorCentavos = recorrencia.valorCentavos,
                    dataLancamento = dataLancamentoString,
                    mesReferencia = mesReferencia,
                    categoria = recorrencia.categoria,
                    observacao = "Materializado automaticamente a partir da regra #${recorrencia.id}",
                    status = StatusLancamento.ATIVO,
                    idLote = recorrencia.idLote,
                    recorrenciaId = recorrencia.id,
                    recorrenciaExcecao = false
                )

                lancamentoRepository.save(novoLancamento)
            }
        }
    }

    @Transactional
    override fun editarSomenteEsteMes(original: Lancamento, req: LancamentoRequest): LancamentoResponse {
        aplicarCamposLancamento(original, req)
        original.recorrenciaExcecao = true

        return lancamentoRepository.save(original).toResponse()
    }

    @Transactional
    override fun editarEsteEProximos(original: Lancamento, req: LancamentoRequest): LancamentoResponse {
        val recorrencia = buscarRecorrencia(original)
        val recorrenciaDestino = separarSerieSeNecessario(recorrencia, req)

        aplicarCamposRecorrencia(recorrenciaDestino, req)
        val recorrenciaSalva = recorrenciaRepository.save(recorrenciaDestino)
        val ocorrencias = lancamentoRepository
            .findAllByRecorrenciaIdAndMesReferenciaGreaterThanEqualAndStatusAndRecorrenciaExcecaoFalseOrderByMesReferenciaAscIdAsc(
                recorrencia.id!!,
                req.mesReferencia
            )

        val atualizada = atualizarOcorrenciasMaterializadas(ocorrencias, recorrenciaSalva, req, original.id)
            ?: atualizarOcorrenciaOriginal(original, recorrenciaSalva, req)

        return atualizada.toResponse()
    }

    @Transactional
    override fun editarTodaSerie(original: Lancamento, req: LancamentoRequest): LancamentoResponse {
        val recorrencia = buscarRecorrencia(original)
        aplicarCamposRecorrencia(recorrencia, req)
        val recorrenciaSalva = recorrenciaRepository.save(recorrencia)
        val ocorrencias = lancamentoRepository
            .findAllByRecorrenciaIdAndStatusAndRecorrenciaExcecaoFalseOrderByMesReferenciaAscIdAsc(recorrencia.id!!)

        val atualizada = atualizarOcorrenciasMaterializadas(ocorrencias, recorrenciaSalva, req, original.id)
            ?: atualizarOcorrenciaOriginal(original, recorrenciaSalva, req)

        return atualizada.toResponse()
    }

    private fun buscarRecorrencia(lancamento: Lancamento): RecorrenciaLancamento {
        val recorrenciaId = lancamento.recorrenciaId
            ?: throw IllegalArgumentException("Lancamento nao pertence a uma recorrencia.")

        return recorrenciaRepository.findById(recorrenciaId)
            .orElseThrow { NoSuchElementException("Recorrencia $recorrenciaId nao encontrada.") }
    }

    private fun separarSerieSeNecessario(
        recorrencia: RecorrenciaLancamento,
        req: LancamentoRequest
    ): RecorrenciaLancamento {
        if (req.mesReferencia <= recorrencia.mesInicio) {
            return recorrencia
        }

        val mesFimOriginal = recorrencia.mesFim
        recorrencia.mesFim = YearMonth.parse(req.mesReferencia).minusMonths(1).toString()
        recorrenciaRepository.save(recorrencia)

        return RecorrenciaLancamento(
            tipo = req.tipo,
            descricao = req.descricao,
            valorCentavos = req.valor.movePointRight(2).toLong(),
            mesInicio = req.mesReferencia,
            mesFim = mesFimOriginal,
            diaPreferencial = req.data.dayOfMonth,
            parcelaInicio = recorrencia.parcelaInicio,
            parcelaTotal = recorrencia.parcelaTotal,
            frequencia = recorrencia.frequencia,
            status = recorrencia.status,
            categoria = req.categoria,
            observacao = req.observacao,
            idLote = req.idLote ?: recorrencia.idLote
        )
    }

    private fun atualizarOcorrenciasMaterializadas(
        ocorrencias: List<Lancamento>,
        recorrencia: RecorrenciaLancamento,
        req: LancamentoRequest,
        idOriginal: Long?
    ): Lancamento? {
        var atualizada: Lancamento? = null

        ocorrencias.forEach { ocorrencia ->
            if (ocorrencia.id == idOriginal) {
                aplicarCamposLancamento(ocorrencia, req)
            } else {
                aplicarCamposOcorrenciaDaSerie(ocorrencia, req)
            }
            ocorrencia.recorrenciaId = recorrencia.id
            ocorrencia.recorrenciaExcecao = false

            if (ocorrencia.id == idOriginal) {
                atualizada = ocorrencia
            }
        }

        lancamentoRepository.saveAll(ocorrencias)
        return atualizada
    }

    private fun atualizarOcorrenciaOriginal(
        original: Lancamento,
        recorrencia: RecorrenciaLancamento,
        req: LancamentoRequest
    ): Lancamento {
        aplicarCamposLancamento(original, req)
        original.recorrenciaId = recorrencia.id
        original.recorrenciaExcecao = false
        return lancamentoRepository.save(original)
    }

    private fun aplicarCamposRecorrencia(recorrencia: RecorrenciaLancamento, req: LancamentoRequest) {
        recorrencia.tipo = req.tipo
        recorrencia.descricao = req.descricao
        recorrencia.valorCentavos = req.valor.movePointRight(2).toLong()
        recorrencia.categoria = req.categoria
        recorrencia.observacao = req.observacao
        recorrencia.idLote = req.idLote ?: recorrencia.idLote
        recorrencia.diaPreferencial = req.data.dayOfMonth
    }

    private fun aplicarCamposLancamento(lancamento: Lancamento, req: LancamentoRequest) {
        lancamento.tipo = req.tipo
        lancamento.idLote = req.idLote
        lancamento.descricao = req.descricao
        lancamento.valorCentavos = req.valor.movePointRight(2).toLong()
        lancamento.dataLancamento = req.data.toString()
        lancamento.mesReferencia = req.mesReferencia
        lancamento.categoria = req.categoria
        lancamento.observacao = req.observacao
    }

    private fun aplicarCamposOcorrenciaDaSerie(lancamento: Lancamento, req: LancamentoRequest) {
        lancamento.tipo = req.tipo
        lancamento.idLote = req.idLote
        lancamento.descricao = req.descricao
        lancamento.valorCentavos = req.valor.movePointRight(2).toLong()
        lancamento.dataLancamento = montarData(lancamento.mesReferencia, req.data.dayOfMonth)
        lancamento.categoria = req.categoria
        lancamento.observacao = req.observacao
    }

    private fun montarData(mes: String, dia: Int): String {
        val yearMonth = YearMonth.parse(mes)

        val diaSeguro = if (dia > yearMonth.lengthOfMonth()) {
            yearMonth.lengthOfMonth()
        } else {
            dia
        }

        val dataCompleta = yearMonth.atDay(diaSeguro)

        return dataCompleta.toString()
    }
}
