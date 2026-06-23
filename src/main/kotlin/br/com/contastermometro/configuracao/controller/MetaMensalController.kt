package br.com.contastermometro.configuracao.controller

import br.com.contastermometro.configuracao.dto.MetaMensalRequest
import br.com.contastermometro.configuracao.dto.MetaMensalResponse
import br.com.contastermometro.configuracao.service.MetaMensalService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/configuracao/metas")
class MetaMensalController(
    private val metaMensalService: MetaMensalService
) {

    @GetMapping
    fun buscar(
        @DateTimeFormat(pattern = "yyyy-MM")
        @RequestParam("mes") mesRaw: YearMonth
    ): ResponseEntity<MetaMensalResponse> {
        return ResponseEntity.ok(metaMensalService.buscar(mesRaw))
    }

    @PostMapping
    fun definir(
        @DateTimeFormat(pattern = "yyyy-MM")
        @RequestParam("mes") mesRaw: YearMonth,
        @Valid @RequestBody request: MetaMensalRequest
    ): ResponseEntity<MetaMensalResponse> {
        val novaMeta = metaMensalService.definir(mesRaw, request)
        return ResponseEntity.ok(novaMeta)
    }

    @PutMapping("/{id}")
    fun editar(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: MetaMensalRequest
    ): ResponseEntity<MetaMensalResponse> {
        val metaAtualizada = metaMensalService.editar(id, request)
        return ResponseEntity.ok(metaAtualizada)
    }
}