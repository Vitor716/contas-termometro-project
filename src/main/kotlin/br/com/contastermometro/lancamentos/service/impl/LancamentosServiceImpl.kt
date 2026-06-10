
package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.mapper.LancamentosMapper
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import org.springframework.stereotype.Service

@Service
class LancamentosServiceImpl(
    private val repository: LancamentoRepository,
    private val mapper: LancamentosMapper,
) : LancamentosService {

    override fun criar(request: LancamentoRequest): LancamentoResponse {
        val entity = mapper.from(request)
        val saved = repository.save(entity)
        return mapper.to(saved)
    }
}