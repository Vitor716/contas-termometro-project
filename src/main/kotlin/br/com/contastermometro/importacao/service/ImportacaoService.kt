package br.com.contastermometro.importacao.service

import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

interface ImportacaoService {
    fun importarLancamentosNubank(@RequestParam("file") file: MultipartFile) : ResultadoImportacao
    fun buscar(): List<LoteImportacaoResponse>
    fun buscarPorId(idLote: String): LoteImportacaoResponse
    fun deletar(idLote: String)
}