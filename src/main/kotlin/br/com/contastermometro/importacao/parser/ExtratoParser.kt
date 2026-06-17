package br.com.contastermometro.importacao.parser

import br.com.contastermometro.importacao.dto.ResultadoImportacao

interface ExtratoParser {
    fun extrair(arquivo: String): ResultadoImportacao
}