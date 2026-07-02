package br.com.contastermometro.lancamentos.controller

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.RemoverLancamentosRequest
import br.com.contastermometro.lancamentos.service.LancamentosService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/lancamentos")
class LancamentosController(
    private val lancamentosService: LancamentosService
) {

    @PostMapping
    fun criar(@Valid @RequestBody req: LancamentoRequest): ResponseEntity<LancamentoResponse> {
        val created = lancamentosService.criar(req)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<LancamentoResponse> {
        return ResponseEntity.ok(lancamentosService.buscar(id))
    }

    @GetMapping()
    fun listarPorMes(
        @DateTimeFormat(pattern = "yyyy-MM")
        @RequestParam("mes") mesRaw: YearMonth
    ): ResponseEntity<List<LancamentoResponse>> {
        return ResponseEntity.ok(lancamentosService.listarPorMes(mesRaw))
    }

    @GetMapping("/lotes/{idLote}")
    fun listarImportacao(@PathVariable idLote: String) : ResponseEntity<List<LancamentoResponse>>{
        val importacao = lancamentosService.listarImportacao(idLote)
        return ResponseEntity.ok(importacao)
    }

    @PutMapping("/{id}")
    fun editar(
        @PathVariable id: Long,
        @Valid @RequestBody req: LancamentoRequest
    ): ResponseEntity<LancamentoResponse> {
        val updated = lancamentosService.editar(id, req)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun remover(@PathVariable id: Long): ResponseEntity<Void> {
        lancamentosService.remover(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/lote")
    fun removerEmLote(@RequestBody request: RemoverLancamentosRequest): ResponseEntity<Void> {
        lancamentosService.removerEmLote(request.ids)
        return ResponseEntity.noContent().build()
    }
}
