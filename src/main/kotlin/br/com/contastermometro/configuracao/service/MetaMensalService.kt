package br.com.contastermometro.configuracao.service

import br.com.contastermometro.configuracao.dto.MetaMensalRequest
import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import java.time.YearMonth

interface MetaMensalService {
    fun criar(mesRaw: YearMonth, request: MetaMensalRequest): MetaMensalResponse
    fun buscar(mesRaw: YearMonth): MetaMensalResponse
    fun editar(id: Long, request: MetaMensalRequest): MetaMensalResponse
}