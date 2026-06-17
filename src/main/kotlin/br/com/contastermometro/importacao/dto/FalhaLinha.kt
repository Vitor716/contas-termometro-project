package br.com.contastermometro.importacao.dto

data class FalhaLinha (
    val numeroLinha: Int,
    val conteudoBruto: String,
    val motivoErro: String,
)