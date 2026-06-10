package br.com.contastermometro.lancamentos.mapper

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.model.Lancamento

interface LancamentosMapper {
	fun to(model: Lancamento): LancamentoResponse
	fun from(request: LancamentoRequest): Lancamento
}