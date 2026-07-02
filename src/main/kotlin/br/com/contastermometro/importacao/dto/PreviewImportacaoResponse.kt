package br.com.contastermometro.importacao.dto

data class PreviewImportacaoResponse(
    val loteId: String,
    val origem: String,
    val status: StatusLoteImportacao,
    val hashArquivo: String,
    val totalProcessado: Int,
    val linhas: List<LinhaImportacaoPreviewResponse>,
)
