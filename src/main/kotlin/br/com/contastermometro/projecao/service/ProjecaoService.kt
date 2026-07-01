package br.com.contastermometro.projecao.service

import br.com.contastermometro.projecao.dto.ProjecaoMensalResponse

interface ProjecaoService {
    fun projetarMeses(mesInicioStr: String, quantidade: Int): List<ProjecaoMensalResponse>
}