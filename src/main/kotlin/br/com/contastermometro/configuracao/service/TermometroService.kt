package br.com.contastermometro.configuracao.service

import br.com.contastermometro.configuracao.dto.TermometroRequest
import br.com.contastermometro.configuracao.dto.TermometroResponse

interface TermometroService {
    fun criar(req: TermometroRequest) : TermometroResponse
    fun buscar() : TermometroResponse
    fun editar(id: Long, req: TermometroRequest): TermometroResponse
}