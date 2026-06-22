package br.com.contastermometro.importacao.dto

import java.time.Instant
import java.time.format.DateTimeFormatter

data class LoteImportacaoResponse(
    var idLote: String = "",
    var origem: String = "",
    var qtdSucessos: Int = 0,
    var qtdFalhas: Int = 0,
    var totalProcessado: Int = 0,
    var logFalhasJson: String? = null,
    var criadoEm: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
)

fun LoteImportacao.toResponse(): LoteImportacaoResponse {
    return LoteImportacaoResponse(
        idLote = idLote,
        origem = origem,
        qtdSucessos = qtdSucessos,
        qtdFalhas = qtdFalhas,
        totalProcessado = totalProcessado,
        logFalhasJson = logFalhasJson,
    )
}