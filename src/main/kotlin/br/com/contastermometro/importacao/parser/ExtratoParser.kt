package br.com.contastermometro.importacao.parser

import br.com.contastermometro.importacao.dto.LinhaImportacaoPreview

interface ExtratoParser {
    fun extrair(arquivo: String): List<LinhaImportacaoPreview>
}
