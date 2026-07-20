package br.com.contastermometro.lancamentos.service

import br.com.contastermometro.lancamentos.dto.AnteciparParcelamentoRequest
import br.com.contastermometro.lancamentos.dto.CancelarRecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaResponse
import java.time.YearMonth

interface RecorrenciaLancamentoService {
    fun criar(request: RecorrenciaRequest): RecorrenciaResponse
    fun buscar(): List<RecorrenciaResponse>
    fun listarPorMes(mesRaw: YearMonth): List<RecorrenciaResponse>
    fun listarParcelamentos(categoria: String): List<RecorrenciaResponse>
    fun editar(id: Long, request: RecorrenciaRequest): RecorrenciaResponse
    fun cancelar(id: Long, request: CancelarRecorrenciaRequest): RecorrenciaResponse
    fun antecipar(id: Long, request: AnteciparParcelamentoRequest): RecorrenciaResponse
    fun remover(id: Long)
}
