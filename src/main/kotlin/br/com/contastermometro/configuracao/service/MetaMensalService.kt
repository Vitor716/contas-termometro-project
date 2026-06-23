package br.com.contastermometro.configuracao.service

import br.com.contastermometro.configuracao.dto.MetaMensalRequest
import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import java.time.YearMonth

interface MetaMensalService {
    fun definirMeta(mesRaw: YearMonth, request: MetaMensalRequest): MetaMensalResponse
    fun buscarMetaVigente(mesRaw: YearMonth): MetaMensalResponse
}