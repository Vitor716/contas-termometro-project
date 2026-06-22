package br.com.contastermometro.importacao.service.impl

import br.com.contastermometro.importacao.dto.LoteImportacao
import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.ResultadoImportacao
import br.com.contastermometro.importacao.dto.toResponse
import br.com.contastermometro.importacao.parser.ExtratoParser
import br.com.contastermometro.importacao.repository.LoteImportacaoRepository
import br.com.contastermometro.importacao.service.ImportacaoService
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.service.LancamentosService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ImportacaoServiceImpl (
    private val extratoParser : ExtratoParser,
    private val lancamentosService: LancamentosService,
    private val repository: LoteImportacaoRepository,
    private val lancamentoRepository: LancamentoRepository
) : ImportacaoService {

    override fun importarLancamentosNubank(file: MultipartFile) : ResultadoImportacao{
        val arquivo = file.inputStream.bufferedReader().readText()
        val lancamentos = extratoParser.extrair(arquivo)

        val idLote = UUID.randomUUID().toString()
        val dataHoraFormatada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val hashImportacao = "lote_${dataHoraFormatada}_${idLote.take(8)}"

        lancamentos.sucessos.forEach { lancamentoRequest ->
            lancamentoRequest.idLote = hashImportacao
            lancamentosService.criar(lancamentoRequest)
        }

        val loteImportacao = LoteImportacao(
            idLote = hashImportacao,
            origem = "Nubank",
            qtdSucessos = lancamentos.sucessos.size,
            qtdFalhas = lancamentos.falhas.size,
            totalProcessado = lancamentos.totalProcessado,
            logFalhasJson = if (lancamentos.falhas.isNotEmpty()) {
                val falhasJson = lancamentos.falhas.joinToString(separator = ",") { falha ->
                    """{"numeroLinha": ${falha.numeroLinha}, "conteudoBruto": "${falha.conteudoBruto}", "motivoErro": "${falha.motivoErro}"}"""
                }
                "[$falhasJson]"
            } else {
                null
            }
        )

        repository.save(loteImportacao)

        return lancamentos
    }

    override fun buscar() : List<LoteImportacaoResponse> {
        val entities = repository.findAll()
        return entities.map { it.toResponse() }
    }

    override fun buscarPorId(idLote: String) : LoteImportacaoResponse {
        val entities = repository.findById(idLote)
            .orElseThrow { RuntimeException("Lote de importação com id $idLote não encontrado.") }
        return entities.toResponse()
    }

    @Transactional
    override fun deletar(idLote: String) {
        val lote = repository.findById(idLote)
            .orElseThrow { RuntimeException("Lote de importação com id $idLote não encontrado.") }
        lancamentoRepository.deleteByIdLoteApenasNoBanco(idLote)
        repository.delete(lote)
    }
}