
package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.*
import br.com.contastermometro.lancamentos.enums.EscopoEdicao
import br.com.contastermometro.lancamentos.model.Lancamento
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.lancamentos.service.MaterializadorRecorrenciaService
import br.com.contastermometro.shared.LancamentoNaoEncontradoException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class LancamentosServiceImpl(
    private val repository: LancamentoRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val materializadorRecorrenciaService: MaterializadorRecorrenciaService
) : LancamentosService {

    override fun criar(request: LancamentoRequest): LancamentoResponse {
        val entity = request.toModel()
        val saved = repository.save(entity)

        eventPublisher.publishEvent(
            LancamentoRegistradoEvent(
                id = saved.id.toString(),
                descricao = saved.descricao,
                valor = saved.valorCentavos.toBigDecimal().movePointLeft(2),
                data = saved.dataLancamento
            )
        )

        return saved.toResponse()
    }

    override fun buscar(id: Long): LancamentoResponse {
        val entity = repository.findById(id)
            .orElseThrow { LancamentoNaoEncontradoException("Lançamento com id $id não encontrado.") }
        return entity.toResponse()
    }

    override fun listarPorMes(mesRaw: YearMonth): List<LancamentoResponse> {
        val mes = mesRaw.toString()

        materializadorRecorrenciaService.garantirRecorrenciasMaterializadasNoMes(mes)

        val entities = repository.findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(mes)
        return entities.map { it.toResponse() }
    }

    override fun listarImportacao(idLote: String): List<LancamentoResponse> {
        val entities = repository.findByIdLote(idLote)
        return entities.map { it.toResponse() }
    }

    override fun remover(id: Long) {
        val lancamento = buscar(id)
        repository.deleteById(lancamento.id)
    }

    override fun removerEmLote(ids: List<Long>) {
        val idsUnicos = ids.distinct()
        if (idsUnicos.isEmpty()) {
            throw IllegalArgumentException("Informe ao menos um lancamento para remover.")
        }
        repository.deleteAllById(idsUnicos)
    }

    override fun editar(id: Long, req: LancamentoRequest): LancamentoResponse {
        val existente = repository.findById(id)
            .orElseThrow { LancamentoNaoEncontradoException("Lançamento $id não encontrado.") }

        if (existente.recorrenciaId != null) {
            return editarLancamentoRecorrente(existente, req)
        }

        atualizarCampos(existente, req)
        return repository.save(existente).toResponse()
    }

    private fun editarLancamentoRecorrente(
        existente: Lancamento,
        req: LancamentoRequest
    ): LancamentoResponse {
        return when (req.escopoEdicao ?: EscopoEdicao.ESTE_MES) {
            EscopoEdicao.ESTE_MES -> materializadorRecorrenciaService.editarSomenteEsteMes(existente, req)
            EscopoEdicao.ESTE_E_PROXIMOS -> materializadorRecorrenciaService.editarEsteEProximos(existente, req)
            EscopoEdicao.TODA_A_SERIE -> materializadorRecorrenciaService.editarTodaSerie(existente, req)
        }
    }

    private fun atualizarCampos(entidade: Lancamento, req: LancamentoRequest) {
        entidade.apply {
            idLote = req.idLote
            tipo = req.tipo
            descricao = req.descricao
            valorCentavos = req.valor.movePointRight(2).toLong()
            dataLancamento = req.data.toString()
            mesReferencia = req.mesReferencia
            categoria = req.categoria
            observacao = req.observacao
        }
    }
}
