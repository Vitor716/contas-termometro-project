package br.com.contastermometro.orcamento.controller

import br.com.contastermometro.orcamento.dto.SnapshotTermometroMensal
import br.com.contastermometro.orcamento.service.SnapshotTermometroService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/configuracoes/termometros/snapshots")
class SnapshotTermometroController(
    private val snapshotTermometroService: SnapshotTermometroService
) {

    @GetMapping("/{mes}")
    fun buscarPorMes(@PathVariable("mes") mes: String): ResponseEntity<SnapshotTermometroMensal> {
        val snapshot = snapshotTermometroService.buscarPorId(mes)
        return ResponseEntity.ok(snapshot)
    }
}
