package br.com.contastermometro.lancamentos.exportacao

data class ArquivoCsvLancamentos(
    val nomeArquivo: String,
    val conteudo: ByteArray,
)
