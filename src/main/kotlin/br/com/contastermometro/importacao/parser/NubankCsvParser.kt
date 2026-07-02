package br.com.contastermometro.importacao.parser

import br.com.contastermometro.importacao.dto.LinhaImportacaoPreview
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Service
class NubankCsvParser : ExtratoParser {

    override fun extrair(arquivo: String): List<LinhaImportacaoPreview> {
        val previews = mutableListOf<LinhaImportacaoPreview>()

        arquivo.lines().drop(1).forEachIndexed { index, linhaBruta ->
            val linhaLimpa = linhaBruta.trim()
            if (linhaLimpa.isBlank()) return@forEachIndexed

            try {
                previews.add(processarLinha(linhaLimpa))
            } catch (ex: Exception) {
                val numeroLinha = index + 2
                throw IllegalArgumentException("Linha $numeroLinha: ${ex.message ?: "erro desconhecido na conversao"}")
            }
        }

        return previews
    }

    private fun processarLinha(linha: String): LinhaImportacaoPreview {
        val colunas = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
        require(colunas.size >= 3) { "A linha contem apenas ${colunas.size} colunas. Esperado no minimo 3." }

        val dataRaw = colunas[0].trim()
        val descricaoRaw = colunas[1].trim().removeSurrounding("\"")
        val valorRaw = colunas[2].trim()

        require(descricaoRaw.isNotBlank()) { "A descricao nao pode estar vazia" }

        val dataParsed = try {
            LocalDate.parse(dataRaw)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Formato de data invalido. Esperado YYYY-MM-DD, recebido: '$dataRaw'")
        }

        val valorLimpo = valorRaw.replace("\"", "").replace(" ", "").replace("R$", "")
        val valorSanitizado = seTiverVirgulaFormatarParaBrasileiro(valorLimpo)
        val valorParsed = valorSanitizado.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Valor numerico invalido: '$valorRaw'")

        val valorCentavos = valorParsed.abs().movePointRight(2).setScale(0).longValueExact()
        val parcelamento = detectarParcelamento(descricaoRaw)
        val categoria = when {
            isPagamentoFatura(parcelamento.descricaoLimpa) -> "PAGAMENTO_FATURA"
            isDescontoAntecipacao(parcelamento.descricaoLimpa) -> "DESCONTO_ANTECIPACAO"
            parcelamento.isParcelamento -> "PARCELAMENTO"
            contemAssinatura(parcelamento.descricaoLimpa) -> "SAIDA_FIXA"
            else -> "Cartao Nubank"
        }

        return LinhaImportacaoPreview(
            descricaoOriginal = descricaoRaw,
            descricaoLimpa = parcelamento.descricaoLimpa,
            valorCentavos = valorCentavos,
            data = dataParsed.toString(),
            hashLinha = hashLinha(dataParsed, valorCentavos, parcelamento),
            categoriaSugerida = categoria,
            isParcelamento = parcelamento.isParcelamento,
            parcelaAtual = parcelamento.parcelaAtual,
            parcelaTotal = parcelamento.parcelaTotal,
        )
    }

    private fun detectarParcelamento(descricao: String): DadosParcelamento {
        val regex = Regex("""(?i)(?:\s*-\s*)?(?:parcela\s*)?(\d{1,2})\s*/\s*(\d{1,2})\s*$""")
        val match = regex.find(descricao)
            ?: return DadosParcelamento(descricaoLimpa = descricao.trim())

        val parcelaAtual = match.groupValues[1].toInt()
        val parcelaTotal = match.groupValues[2].toInt()
        require(parcelaAtual > 0 && parcelaTotal > 0 && parcelaAtual <= parcelaTotal) {
            "Parcelamento invalido: $parcelaAtual/$parcelaTotal"
        }

        return DadosParcelamento(
            descricaoLimpa = descricao.replace(regex, "").trim().trimEnd('-', ' '),
            isParcelamento = true,
            parcelaAtual = parcelaAtual,
            parcelaTotal = parcelaTotal,
        )
    }

    private fun contemAssinatura(descricao: String): Boolean {
        val texto = descricao.lowercase()
        return listOf("netflix", "spotify").any { texto.contains(it) }
    }

    private fun isPagamentoFatura(descricao: String): Boolean {
        return descricao.lowercase().contains("pagamento recebido")
    }

    private fun isDescontoAntecipacao(descricao: String): Boolean {
        return descricao.lowercase().contains("desconto antecip")
    }

    private fun hashLinha(data: LocalDate, valorCentavos: Long, parcelamento: DadosParcelamento): String {
        val parcela = if (parcelamento.isParcelamento) {
            "|${parcelamento.parcelaAtual}/${parcelamento.parcelaTotal}"
        } else {
            ""
        }
        val conteudo = "${data}|${valorCentavos}|${parcelamento.descricaoLimpa.lowercase()}${parcela}"
        return MessageDigest
            .getInstance("SHA-256")
            .digest(conteudo.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun seTiverVirgulaFormatarParaBrasileiro(valor: String): String {
        if (valor.contains(",")) {
            return valor
                .replace(".", "")
                .replace(",", ".")
        }
        return valor
    }

    private data class DadosParcelamento(
        val descricaoLimpa: String,
        val isParcelamento: Boolean = false,
        val parcelaAtual: Int? = null,
        val parcelaTotal: Int? = null,
    )
}
