package br.com.contastermometro.consultor.service

import br.com.contastermometro.consultor.dto.SimularCompraRequest
import br.com.contastermometro.consultor.dto.SimularCompraResponse

interface ConsultorService {
    fun simularCompra(request: SimularCompraRequest): SimularCompraResponse
}