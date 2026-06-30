package br.com.contastermometro.lancamentos.service

import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaResponse
import org.springframework.http.ResponseEntity

interface RecorrenciaLancamentoService {
    fun criar(request: RecorrenciaRequest): RecorrenciaResponse
    fun buscar(): List<RecorrenciaResponse>
}