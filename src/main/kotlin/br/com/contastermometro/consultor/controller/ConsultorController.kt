package br.com.contastermometro.consultor.controller

import br.com.contastermometro.consultor.dto.SimularCompraRequest
import br.com.contastermometro.consultor.dto.SimularCompraResponse
import br.com.contastermometro.consultor.service.ConsultorService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/consultor")
class ConsultorController(private val consultorService: ConsultorService) {

    @PostMapping("/simular-compra")
    fun simularCompra(
        @Valid @RequestBody request: SimularCompraRequest
    ): ResponseEntity<SimularCompraResponse> =
        ResponseEntity.ok(consultorService.simularCompra(request))
}