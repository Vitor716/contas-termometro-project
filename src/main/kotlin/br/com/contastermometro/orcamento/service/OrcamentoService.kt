package br.com.contastermometro.orcamento.service

import br.com.contastermometro.orcamento.dto.ResumoMensal
import java.time.LocalDate

interface OrcamentoService {
    fun gerarResumoMensal(mesRaw: LocalDate): ResumoMensal
}