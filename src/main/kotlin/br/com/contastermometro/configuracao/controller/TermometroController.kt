package br.com.contastermometro.configuracao.controller

import br.com.contastermometro.configuracao.dto.TermometroRequest
import br.com.contastermometro.configuracao.dto.TermometroResponse
import br.com.contastermometro.configuracao.service.TermometroService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/configuracao/termometro")
class TermometroController (
    private val service : TermometroService
){
    @PostMapping
    fun criar(@Valid @RequestBody request: TermometroRequest): ResponseEntity<TermometroResponse> {
        val created = service.criar(request)
        return ResponseEntity.status(201).body(created)
    }

    @GetMapping()
    fun buscar(): ResponseEntity<TermometroResponse> {
        return ResponseEntity.ok(service.buscar())
    }

    @PutMapping("/{id}")
    fun editar(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: TermometroRequest
    ) : ResponseEntity<TermometroResponse> {
        val updatedTermometro = service.editar(id, request)
        return ResponseEntity.ok(updatedTermometro)
    }
}