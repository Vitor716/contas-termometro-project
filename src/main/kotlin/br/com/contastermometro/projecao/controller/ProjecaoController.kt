package br.com.contastermometro.projecao.controller

import br.com.contastermometro.projecao.dto.ProjecaoMensalResponse
import br.com.contastermometro.projecao.service.ProjecaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/projecoes")
class ProjecaoController(private val projecaoService: ProjecaoService) {

    @GetMapping("/mensal")
    fun projecaoMensal(
        @RequestParam("mesInicio", required = false) mesInicio: String?,
        @RequestParam("meses", defaultValue = "6") meses: Int
    ): ResponseEntity<List<ProjecaoMensalResponse>> {
        val inicio = mesInicio ?: YearMonth.now().plusMonths(1).toString()
        return ResponseEntity.ok(projecaoService.projetarMeses(inicio, meses.coerceIn(1, 24)))
    }
}