package br.com.contastermometro.lancamentos.controller

import br.com.contastermometro.lancamentos.dto.AnteciparParcelamentoRequest
import br.com.contastermometro.lancamentos.dto.CancelarRecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaResponse
import br.com.contastermometro.lancamentos.service.RecorrenciaLancamentoService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/recorrencias")
class RecorrenciaLancamentoController(
    private val recorrenciaService: RecorrenciaLancamentoService
) {
    @PostMapping
    fun criar(@Valid @RequestBody request: RecorrenciaRequest): ResponseEntity<RecorrenciaResponse> {
        val created = recorrenciaService.criar(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping()
    fun buscar(): ResponseEntity<List<RecorrenciaResponse>> {
        val list = recorrenciaService.buscar()
        return ResponseEntity.ok(list)
    }

    @GetMapping("/mensal")
    fun listarPorMes(
        @DateTimeFormat(pattern = "yyyy-MM")
        @RequestParam("mes") mesRaw: YearMonth
    ): ResponseEntity<List<RecorrenciaResponse>> {
        return ResponseEntity.ok(recorrenciaService.listarPorMes(mesRaw))
    }

    @GetMapping("/parcelamentos")
    fun listarParcelamentos(
        @RequestParam("categoria") categoria: String
    ): ResponseEntity<List<RecorrenciaResponse>> {
        return ResponseEntity.ok(recorrenciaService.listarParcelamentos(categoria))
    }

    @PutMapping("/{id}")
    fun editar(
        @PathVariable id: Long,
        @Valid @RequestBody request: RecorrenciaRequest
    ): ResponseEntity<RecorrenciaResponse> =
        ResponseEntity.ok(recorrenciaService.editar(id, request))

    @PatchMapping("/{id}/cancelar")
    fun cancelar(
        @PathVariable id: Long,
        @RequestBody(required = false) request: CancelarRecorrenciaRequest?
    ): ResponseEntity<RecorrenciaResponse> {
        val safeRequest = request ?: CancelarRecorrenciaRequest()
        return ResponseEntity.ok(recorrenciaService.cancelar(id, safeRequest))
    }

    @PatchMapping("/{id}/antecipar")
    fun antecipar(
        @PathVariable id: Long,
        @Valid @RequestBody request: AnteciparParcelamentoRequest
    ): ResponseEntity<RecorrenciaResponse> =
        ResponseEntity.ok(recorrenciaService.antecipar(id, request))

    @DeleteMapping("/{id}")
    fun remover(@PathVariable id: Long): ResponseEntity<Void> {
        recorrenciaService.remover(id)
        return ResponseEntity.noContent().build()
    }
}
