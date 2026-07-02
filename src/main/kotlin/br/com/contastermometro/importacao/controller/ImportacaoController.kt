package br.com.contastermometro.importacao.controller

import br.com.contastermometro.importacao.dto.ConfirmarImportacaoRequest
import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.PreviewImportacaoResponse
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/importacao")
class ImportacaoController(
    private val importacaoService: ImportacaoService
) {

    @PostMapping("/preview", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun preview(@RequestParam("file") file: MultipartFile): ResponseEntity<PreviewImportacaoResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(importacaoService.preview(file))
    }

    @PostMapping("/lotes/{loteId}/confirmar", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun confirmar(
        @PathVariable loteId: String,
        @RequestBody request: ConfirmarImportacaoRequest,
    ): ResponseEntity<LoteImportacaoResponse> {
        return ResponseEntity.ok(importacaoService.confirmar(loteId, request))
    }

    @GetMapping
    fun listar(): ResponseEntity<List<LoteImportacaoResponse>> {
        return ResponseEntity.ok(importacaoService.buscar())
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: String): ResponseEntity<LoteImportacaoResponse> {
        return ResponseEntity.ok(importacaoService.buscarPorId(id))
    }

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: String): ResponseEntity<Void> {
        importacaoService.deletar(id)
        return ResponseEntity.noContent().build()
    }
}
