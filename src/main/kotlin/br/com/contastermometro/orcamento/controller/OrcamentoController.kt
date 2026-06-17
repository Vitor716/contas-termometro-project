package br.com.contastermometro.orcamento.controller

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
@RequestMapping("api/orcamento")
class OrcamentoController (
    private val orcamentoService: OrcamentoService
){

    @GetMapping()
    fun gerarResumoMensal(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @RequestParam("mes") mesRaw: LocalDate): ResponseEntity<ResumoMensal> {
     return ResponseEntity.ok(orcamentoService.gerarResumoMensal(mesRaw));
    }
}