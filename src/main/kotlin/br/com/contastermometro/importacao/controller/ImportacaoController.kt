package br.com.contastermometro.importacao.controller

import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/importacoes")
class ImportacaoController(
    private val importacaoService: ImportacaoService
) {

    @PostMapping("/nubank")
    fun importarNubank(@RequestParam("file") file: MultipartFile) : ResponseEntity<ResultadoImportacao> {
        val lancamentos = importacaoService.importarLancamentosNubank(file)
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentos)
    }

    @GetMapping
    fun listar() : ResponseEntity<List<LoteImportacaoResponse>>{
        return ResponseEntity.ok(importacaoService.buscar())
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: String) : ResponseEntity<LoteImportacaoResponse>{
        return ResponseEntity.ok(importacaoService.buscarPorId(id))
    }

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: String) : ResponseEntity<Void>{
        importacaoService.deletar(id)
        return ResponseEntity.noContent().build()
    }
}