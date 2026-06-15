package br.com.contastermometro.lancamentos.service

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import java.time.YearMonth

interface LancamentosService {
	fun criar(request: LancamentoRequest): LancamentoResponse
	fun buscar(id: Long): LancamentoResponse
	fun remover(id: Long)
	fun listarPorMes(mesRaw: YearMonth): List<LancamentoResponse>
}