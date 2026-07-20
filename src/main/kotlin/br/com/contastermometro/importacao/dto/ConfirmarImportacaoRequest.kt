package br.com.contastermometro.importacao.dto

data class ConfirmarImportacaoRequest(
    val linhas: List<LinhaImportacaoRevisadaRequest>
)

data class LinhaImportacaoRevisadaRequest(
    val id: Long,
    val categoria: String,
    val descricao: String? = null,
    val importar: Boolean = true,
)
