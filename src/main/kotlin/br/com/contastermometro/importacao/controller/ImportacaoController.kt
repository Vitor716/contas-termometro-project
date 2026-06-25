package br.com.contastermometro.importacao.controller

import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/importacoes")
class ImportacaoController(
    private val importacaoService: ImportacaoService
) {

    @PostMapping("/nubank")
    fun importarLancamentosNubank(@RequestParam("file") file: MultipartFile) : ResponseEntity<ResultadoImportacao> {
        val lancamentos = importacaoService.importarLancamentosNubank(file)
        return ResponseEntity.ok(lancamentos)
    }

    @GetMapping("/lotes")
    fun buscar() : ResponseEntity<List<LoteImportacaoResponse>>{
        val lotes = importacaoService.buscar()
        return ResponseEntity.ok(lotes)
    }

    @GetMapping("/lotes/{id}")
    fun buscarPorId(@PathVariable id: String) : ResponseEntity<LoteImportacaoResponse>{
        val lotes = importacaoService.buscarPorId(id)
        return ResponseEntity.ok(lotes)
    }

    @DeleteMapping("/lotes/{id}")
    fun deletar(@PathVariable idLote: String) : ResponseEntity<Void>{
        importacaoService.deletar(idLote)
        return ResponseEntity.noContent().build()
    }
}