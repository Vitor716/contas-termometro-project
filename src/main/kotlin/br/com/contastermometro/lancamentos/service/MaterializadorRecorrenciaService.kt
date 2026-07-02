package br.com.contastermometro.lancamentos.service

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.model.Lancamento

interface MaterializadorRecorrenciaService {
    fun garantirRecorrenciasMaterializadasNoMes(mesReferencia: String)
    fun editarSomenteEsteMes(original: Lancamento, req: LancamentoRequest): LancamentoResponse
    fun editarEsteEProximos(original: Lancamento, req: LancamentoRequest): LancamentoResponse
    fun editarTodaSerie(original: Lancamento, req: LancamentoRequest): LancamentoResponse
}
