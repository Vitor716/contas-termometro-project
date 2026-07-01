package br.com.contastermometro.consultor.dto

data class SimularCompraResponse(
    val recomendacao: RecomendacaoCompra,
    val motivacao: String,
    val valorParcelaCentavos: Long,
    val comprometimentoMedioAtualCentavos: Long,
    val comprometimentoMedioComCompraCentavos: Long,
    val mesesCriticos: List<MesCriticoResponse>
)