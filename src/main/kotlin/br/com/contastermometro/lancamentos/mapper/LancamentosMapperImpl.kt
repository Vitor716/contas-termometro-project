package br.com.contastermometro.lancamentos.mapper

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.toModel
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.model.Lancamento

class LancamentosMapperImpl {
    fun to(entity: Lancamento): LancamentoResponse = entity.toResponse()

    fun from(request: LancamentoRequest): Lancamento = request.toModel()
}
