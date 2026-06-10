package br.com.contastermometro.lancamentos.service

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse

interface LancamentosService {
	fun criar(request: LancamentoRequest): LancamentoResponse
}