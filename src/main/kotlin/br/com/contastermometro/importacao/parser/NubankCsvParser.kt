package br.com.contastermometro.importacao.parser

import br.com.contastermometro.importacao.dto.FalhaLinha
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

@Service
class NubankCsvParser() : ExtratoParser {

    override fun extrair(arquivo: String): ResultadoImportacao {
        val linhas = arquivo.lines()
        val sucessos = mutableListOf<LancamentoRequest>()
        val falhas = mutableListOf<FalhaLinha>()

        linhas.drop(1).forEachIndexed { index, linhaBruta ->
            val numeroRealDaLinha = index + 2
            val linhaLimpa = linhaBruta.trim()

            if (linhaBruta.isEmpty() || linhaLimpa.isBlank()) {
                return@forEachIndexed
            }

            try {
                val lancamento = processarLinha(linhaLimpa)
                sucessos.add(lancamento)
            } catch (ex: Exception) {
                falhas.add(
                    FalhaLinha(
                        numeroLinha = numeroRealDaLinha,
                        conteudoBruto = linhaLimpa,
                        motivoErro = ex.message ?: "Erro desconhecido na conversão"
                    )
                )
            }
        }

        return ResultadoImportacao(
            sucessos = sucessos,
            falhas = falhas,
            totalProcessado = sucessos.size + falhas.size
        )
    }

    /**
     * Função privada dedicada exclusivamente a quebrar e validar uma única linha do CSV.
     * Segue o princípio de "Fail Fast" (lança exceção na primeira inconsistência).
     */
    private fun processarLinha(linha: String): LancamentoRequest {
        val colunas = linha.split(",")

        require(colunas.size >= 4) { "A linha não contém as 4 colunas obrigatórias" }

        val dataRaw = colunas[0].trim()
        val descricaoRaw = colunas[1].trim()
        val valorRaw = colunas[2].trim().replace("\"", "")
        val categoriaRaw = colunas[3].trim()

        require(descricaoRaw.isNotBlank()) { "A descrição não pode estar vazia" }

        val dataParsed = try {
            LocalDate.parse(dataRaw)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Formato de data inválido. Esperado YYYY-MM-DD, recebido: '$dataRaw'")
        }

        val valorParsed = valorRaw.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Valor numérico inválido: '$valorRaw'")

        val valorAbsoluto = valorParsed.abs()

        val tipoInferido = if (valorParsed < BigDecimal.ZERO) {
            TipoLancamento.GASTO_DIARIO
        } else {
            TipoLancamento.ENTRADA
        }

        return LancamentoRequest(
            tipo = tipoInferido,
            descricao = descricaoRaw,
            valor = valorAbsoluto,
            data = dataParsed,
            mesReferencia = YearMonth.from(dataParsed).toString(),
            categoria = categoriaRaw.ifBlank { "Sem Categoria" },
            observacao = "Importação Nubank CSV"
        )
    }
}