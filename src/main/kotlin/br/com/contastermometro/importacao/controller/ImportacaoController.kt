package br.com.contastermometro.importacao.controller

import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/importacao")
class ImportacaoController(
    private val importacaoService: ImportacaoService
) {

    @PostMapping("/upload/nubank")
    fun importarLancamentosNubank(@RequestParam("file") file: MultipartFile) : ResponseEntity<ResultadoImportacao> {
        val lancamentos = importacaoService.importarLancamentosNubank(file)
        return ResponseEntity.ok(lancamentos)
    }
}