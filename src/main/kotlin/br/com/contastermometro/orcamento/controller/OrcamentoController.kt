package br.com.contastermometro.orcamento.controller

import br.com.contastermometro.orcamento.dto.ResumoAnualResponse
import br.com.contastermometro.orcamento.dto.ResumoMensal
import br.com.contastermometro.orcamento.service.OrcamentoService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/orcamentos")
class OrcamentoController (
    private val orcamentoService: OrcamentoService
){

    @GetMapping("/mensal")
    fun obterResumoMensal(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @RequestParam("mes") mesRaw: LocalDate
    ): ResponseEntity<ResumoMensal> {
        return ResponseEntity.ok(orcamentoService.gerarResumoMensal(mesRaw));
    }

    @GetMapping("/anual")
    fun obterResumoAnual(
        @RequestParam("ano") ano: Int
    ): ResponseEntity<ResumoAnualResponse> {
        return ResponseEntity.ok(orcamentoService.gerarResumoAnual(ano))
    }
}