package br.com.contastermometro.lancamentos.controller

import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/lancamentos")
class LancamentosController(
    private val lancamentosService: LancamentosService
) {

    @PostMapping
    fun criar(@Valid @RequestBody req: LancamentoRequest): ResponseEntity<Any> {
        val created = lancamentosService.criar(req)
        return ResponseEntity.status(201).body(created)
    }
}