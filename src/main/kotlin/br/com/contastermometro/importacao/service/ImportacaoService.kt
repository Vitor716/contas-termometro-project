package br.com.contastermometro.importacao.service

import br.com.contastermometro.importacao.dto.ConfirmarImportacaoRequest
import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.PreviewImportacaoResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

interface ImportacaoService {
    fun preview(@RequestParam("file") file: MultipartFile): PreviewImportacaoResponse
    fun confirmar(loteId: String, request: ConfirmarImportacaoRequest): LoteImportacaoResponse
    fun buscar(): List<LoteImportacaoResponse>
    fun buscarPorId(idLote: String): LoteImportacaoResponse
    fun deletar(idLote: String)
}
