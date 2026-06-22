
package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.toModel
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.shared.LancamentoNaoEncontradoException
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class LancamentosServiceImpl(
    private val repository: LancamentoRepository
) : LancamentosService {

    override fun criar(request: LancamentoRequest): LancamentoResponse {
        val entity = request.toModel()
        val saved = repository.save(entity)
        return saved.toResponse()
    }

    override fun buscar(id: Long): LancamentoResponse {
        val entity = repository.findById(id)
            .orElseThrow { LancamentoNaoEncontradoException("Lançamento com id $id não encontrado.") }
        return entity.toResponse()
    }

    override fun listarPorMes(mesRaw: YearMonth): List<LancamentoResponse> {
        val mes = mesRaw.toString()
        val entities = repository.findByMesReferencia(mes)
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
        val updatedEntity = repository.findById(id).orElseThrow { LancamentoNaoEncontradoException("Lançamento com id $id não encontrado.") }.apply {
            descricao = req.descricao
            valorCentavos = req.valor.movePointRight(2).toLong()
            dataLancamento = req.data.toString()
            mesReferencia = req.mesReferencia
            categoria = req.categoria
            observacao = req.observacao
        }

        val saved = repository.save(updatedEntity)
        return saved.toResponse()
    }
}