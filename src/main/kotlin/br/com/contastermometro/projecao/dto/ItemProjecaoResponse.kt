package br.com.contastermometro.projecao.dto

import br.com.contastermometro.lancamentos.enums.TipoLancamento

data class ItemProjecaoResponse(
    val id: Long,
    val descricao: String,
    val valorCentavos: Long,
    val tipo: TipoLancamento,
    val categoria: String?,
    val isParcelamento: Boolean,
    val parcelaAtual: Int?,
    val parcelaTotal: Int?
)