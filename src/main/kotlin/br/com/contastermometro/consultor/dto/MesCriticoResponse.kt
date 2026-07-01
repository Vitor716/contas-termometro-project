package br.com.contastermometro.consultor.dto

data class MesCriticoResponse(
    val mes: String,
    val entradasCentavos: Long,
    val comprometimentoAtualCentavos: Long,
    val comprometimentoComCompraCentavos: Long,
    val saldoComCompraCentavos: Long
)