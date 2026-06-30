
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

    override fun editar(id: Long, req: LancamentoRequest): LancamentoResponse {
        val existente = repository.findById(id)
            .orElseThrow { LancamentoNaoEncontradoException("Lançamento $id não encontrado.") }

        if (req.escopoEdicao == EscopoEdicao.ESTE_MES && existente.recorrenciaId != null) {
            return materializadorRecorrenciaService.editarComoExcecao(existente, req)
        }

        atualizarCampos(existente, req)
        return repository.save(existente).toResponse()
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