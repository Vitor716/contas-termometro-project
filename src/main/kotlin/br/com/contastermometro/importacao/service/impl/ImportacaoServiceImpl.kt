package br.com.contastermometro.importacao.service.impl

import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.parser.ExtratoParser
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImportacaoServiceImpl (
    private val extratoParser : ExtratoParser
) : ImportacaoService {

    override fun importarLancamentosNubank(file: MultipartFile) : ResultadoImportacao{
        val arquivo = file.inputStream.bufferedReader().readText()
        val lancamentos = extratoParser.extrair(arquivo)
        return lancamentos
    }
}