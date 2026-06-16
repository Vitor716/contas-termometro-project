package br.com.contastermometro.orcamento.service

import br.com.contastermometro.orcamento.dto.ResumoMensal
import java.time.YearMonth

interface OrcamentoService {
    fun gerarResumoMensal(mesRaw: YearMonth): ResumoMensal
}