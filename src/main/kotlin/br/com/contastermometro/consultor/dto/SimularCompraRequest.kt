package br.com.contastermometro.consultor.dto

data class SimularCompraRequest(
    val descricao: String,
    val valorTotalCentavos: Long,
    val numeroParcelas: Int,
    val mesInicio: String
)