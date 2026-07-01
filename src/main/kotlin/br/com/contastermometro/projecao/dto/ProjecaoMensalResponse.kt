package br.com.contastermometro.projecao.dto

data class ProjecaoMensalResponse(
    val mes: String,
    val entradasRecorrentes: Long,
    val saidasFixas: Long,
    val parcelamentos: Long,
    val outrosGastosRecorrentes: Long,
    val totalCompromissos: Long,
    val saldoProjectado: Long,
    val itens: List<ItemProjecaoResponse>
)