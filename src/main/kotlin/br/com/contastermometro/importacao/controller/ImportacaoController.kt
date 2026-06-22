package br.com.contastermometro.importacao.controller

import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.service.ImportacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @GetMapping("/lotes")
    fun buscar() : ResponseEntity<List<LoteImportacaoResponse>>{
        val lotes = importacaoService.buscar()
        return ResponseEntity.ok(lotes)
    }

    @GetMapping("/lotesPorId")
    fun buscarPorId(idLote: String) : ResponseEntity<LoteImportacaoResponse>{
        val lotes = importacaoService.buscarPorId(idLote)
        return ResponseEntity.ok(lotes)
    }

    @DeleteMapping()
    fun deletar(idLote: String) : ResponseEntity<Void>{
        importacaoService.deletar(idLote)
        return ResponseEntity.noContent().build()
    }
}