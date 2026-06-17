package br.com.contastermometro.importacao.dto

import br.com.contastermometro.lancamentos.dto.LancamentoRequest

data class ResultadoImportacao (
    val sucessos: List<LancamentoRequest>,
    val falhas: List<FalhaLinha>,
    val totalProcessado: Int
)