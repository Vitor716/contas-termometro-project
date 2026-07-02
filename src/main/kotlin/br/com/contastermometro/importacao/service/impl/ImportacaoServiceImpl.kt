package br.com.contastermometro.importacao.service.impl

import br.com.contastermometro.importacao.dto.ConfirmarImportacaoRequest
import br.com.contastermometro.importacao.dto.LoteImportacao
import br.com.contastermometro.importacao.dto.LoteImportacaoResponse
import br.com.contastermometro.importacao.dto.PreviewImportacaoResponse
import br.com.contastermometro.importacao.dto.StatusLoteImportacao
import br.com.contastermometro.importacao.dto.toResponse
import br.com.contastermometro.importacao.parser.ExtratoParser
import br.com.contastermometro.importacao.repository.LinhaImportacaoPreviewRepository
import br.com.contastermometro.importacao.repository.LoteImportacaoRepository
import br.com.contastermometro.importacao.service.ImportacaoService
import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.RecorrenciaRequest
import br.com.contastermometro.lancamentos.enums.Frequencia
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.lancamentos.service.LancamentosService
import br.com.contastermometro.lancamentos.service.RecorrenciaLancamentoService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class ImportacaoServiceImpl(
    private val extratoParser: ExtratoParser,
    private val lancamentosService: LancamentosService,
    private val repository: LoteImportacaoRepository,
    private val previewRepository: LinhaImportacaoPreviewRepository,
    private val lancamentoRepository: LancamentoRepository,
    private val recorrenciaService: RecorrenciaLancamentoService,
    private val recorrenciaRepository: RecorrenciaLancamentoRepository
) : ImportacaoService {

    @Transactional
    override fun preview(file: MultipartFile): PreviewImportacaoResponse {
        val bytes = file.bytes
        val arquivo = bytes.toString(Charsets.UTF_8)
        val hashArquivo = sha256(bytes)

        if (repository.existsByHashArquivo(hashArquivo)) {
            throw IllegalArgumentException("Arquivo ja importado anteriormente.")
        }

        val idLote = UUID.randomUUID().toString()
        val dataHoraFormatada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val codigoLote = "lote_${dataHoraFormatada}_${idLote.take(8)}"

        val linhas = extratoParser.extrair(arquivo).map { linha ->
            linha.loteId = codigoLote
            linha.isDuplicidade = lancamentoRepository.existsByDescricaoAndValorCentavosAndDataLancamento(
                descricaoParaLancamento(linha.descricaoLimpa, linha.isParcelamento, linha.parcelaAtual, linha.parcelaTotal),
                linha.valorCentavos,
                linha.data
            )
            linha
        }

        val loteImportacao = LoteImportacao(
            idLote = codigoLote,
            origem = "Nubank",
            qtdSucessos = 0,
            qtdFalhas = 0,
            totalProcessado = linhas.size,
            status = StatusLoteImportacao.PENDENTE,
            hashArquivo = hashArquivo,
        )

        repository.save(loteImportacao)
        val linhasSalvas = previewRepository.saveAll(linhas)

        return PreviewImportacaoResponse(
            loteId = codigoLote,
            origem = loteImportacao.origem,
            status = loteImportacao.status,
            hashArquivo = hashArquivo,
            totalProcessado = linhasSalvas.size,
            linhas = linhasSalvas.map { it.toResponse() },
        )
    }

    @Transactional
    override fun confirmar(loteId: String, request: ConfirmarImportacaoRequest): LoteImportacaoResponse {
        val lote = repository.findById(loteId)
            .orElseThrow { RuntimeException("Lote de importacao com id $loteId nao encontrado.") }

        if (lote.status == StatusLoteImportacao.CONFIRMADO) {
            throw IllegalStateException("Lote ja confirmado.")
        }

        val revisoes = request.linhas.associateBy { it.id }
        val linhas = previewRepository.findByLoteId(loteId)
        val linhasConfirmadas = mutableListOf<LinhaConfirmadaParcelamento>()
        var importadas = 0

        linhas.forEach { linha ->
            val revisao = revisoes[linha.id] ?: return@forEach
            if (!revisao.importar) return@forEach

            val data = LocalDate.parse(linha.data)
            val categoriaFinal = revisao.categoria.ifBlank { linha.categoriaSugerida ?: "Cartao Nubank" }
            lancamentosService.criar(
                LancamentoRequest(
                    tipo = tipoLancamento(categoriaFinal),
                    idLote = loteId,
                    descricao = descricaoParaLancamento(
                        linha.descricaoLimpa,
                        linha.isParcelamento,
                        linha.parcelaAtual,
                        linha.parcelaTotal
                    ),
                    valor = linha.valorCentavos.toBigDecimal().movePointLeft(2),
                    data = data,
                    mesReferencia = YearMonth.from(data).toString(),
                    categoria = categoriaFinal,
                    observacao = "Importacao Nubank CSV"
                )
            )

            if (linha.isParcelamento && categoriaFinal == "PARCELAMENTO") {
                linhasConfirmadas.add(
                    LinhaConfirmadaParcelamento(
                        descricaoLimpa = linha.descricaoLimpa,
                        valorCentavos = linha.valorCentavos,
                        data = data,
                        loteId = loteId,
                        parcelaAtual = linha.parcelaAtual,
                        parcelaTotal = linha.parcelaTotal,
                    )
                )
            }
            importadas++
        }

        reconciliarCompromissosParcelados(linhasConfirmadas)

        previewRepository.deleteByLoteId(loteId)
        lote.qtdSucessos = importadas
        lote.qtdFalhas = linhas.size - importadas
        lote.totalProcessado = linhas.size
        lote.status = StatusLoteImportacao.CONFIRMADO

        return repository.save(lote).toResponse()
    }

    override fun buscar(): List<LoteImportacaoResponse> {
        return repository.findAll().map { it.toResponse() }
    }

    override fun buscarPorId(idLote: String): LoteImportacaoResponse {
        val entity = repository.findById(idLote)
            .orElseThrow { RuntimeException("Lote de importacao com id $idLote nao encontrado.") }
        return entity.toResponse()
    }

    @Transactional
    override fun deletar(idLote: String) {
        val lote = repository.findById(idLote)
            .orElseThrow { RuntimeException("Lote de importacao com id $idLote nao encontrado.") }
        previewRepository.deleteByLoteId(idLote)
        val recorrenciaIds = recorrenciaRepository.findByIdLote(idLote).mapNotNull { it.id }
        if (recorrenciaIds.isNotEmpty()) {
            lancamentoRepository.deleteByRecorrenciaIdIn(recorrenciaIds)
        }
        recorrenciaRepository.deleteByIdLote(idLote)
        lancamentoRepository.deleteByIdLoteApenasNoBanco(idLote)
        repository.delete(lote)
    }

    private fun sha256(bytes: ByteArray): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    private fun tipoLancamento(categoria: String): TipoLancamento {
        return when (categoria) {
            "PAGAMENTO_FATURA", "DESCONTO_ANTECIPACAO" -> TipoLancamento.AJUSTE_SALDO
            else -> TipoLancamento.GASTO_DIARIO
        }
    }

    private fun descricaoParaLancamento(
        descricaoLimpa: String,
        isParcelamento: Boolean,
        parcelaAtual: Int?,
        parcelaTotal: Int?
    ): String {
        if (!isParcelamento || parcelaAtual == null || parcelaTotal == null) {
            return descricaoLimpa
        }
        return "$descricaoLimpa $parcelaAtual/$parcelaTotal"
    }

    private fun reconciliarCompromissosParcelados(linhas: List<LinhaConfirmadaParcelamento>) {
        linhas
            .filter { it.parcelaAtual != null && it.parcelaTotal != null }
            .groupBy { "${normalizarChaveCompra(it.descricaoLimpa)}|${it.valorCentavos}|${it.parcelaTotal}" }
            .values
            .forEach { grupo ->
                val parcelasImportadas = grupo.mapNotNull { it.parcelaAtual }.distinct().sorted()
                val primeiraParcelaImportada = parcelasImportadas.firstOrNull() ?: return@forEach
                val ultimaParcelaImportada = parcelasImportadas.last()
                val totalParcelas = grupo.first().parcelaTotal ?: return@forEach

                val dataReferencia = grupo.maxBy { it.parcelaAtual ?: 0 }.data
                val mesesCobertosNoLote = parcelasImportadas.size
                val mesInicio = YearMonth.from(dataReferencia).plusMonths(mesesCobertosNoLote.toLong()).toString()
                val parcelasRestantes = totalParcelas - ultimaParcelaImportada
                val recorrenciaExistente = buscarParcelamentoAtivoCorrespondente(grupo.first())

                if (recorrenciaExistente != null) {
                    removerMaterializacoesQuitadas(recorrenciaExistente.id, YearMonth.from(dataReferencia), YearMonth.parse(mesInicio))
                }

                if (parcelasRestantes <= 0) {
                    recorrenciaExistente?.apply {
                        mesFim = YearMonth.from(dataReferencia).toString()
                        status = StatusParcelamento.INATIVO
                        observacao = observacaoComAntecipacao(grupo, primeiraParcelaImportada, ultimaParcelaImportada, totalParcelas)
                    }?.also { recorrenciaRepository.save(it) }
                    return@forEach
                }

                val mesFim = YearMonth.parse(mesInicio).plusMonths((parcelasRestantes - 1).toLong()).toString()
                val observacao = observacaoComAntecipacao(grupo, primeiraParcelaImportada, ultimaParcelaImportada, totalParcelas)

                if (recorrenciaExistente != null) {
                    recorrenciaExistente.mesInicio = mesInicio
                    recorrenciaExistente.mesFim = mesFim
                    recorrenciaExistente.diaPreferencial = dataReferencia.dayOfMonth
                    recorrenciaExistente.parcelaInicio = ultimaParcelaImportada + 1
                    recorrenciaExistente.parcelaTotal = totalParcelas
                    recorrenciaExistente.status = StatusParcelamento.ATIVO
                    recorrenciaExistente.observacao = observacao
                    recorrenciaRepository.save(recorrenciaExistente)
                    return@forEach
                }

                recorrenciaService.criar(
                    RecorrenciaRequest(
                        tipo = TipoLancamento.GASTO_DIARIO,
                        descricao = grupo.first().descricaoLimpa,
                        valorCentavos = grupo.first().valorCentavos,
                        categoria = "PARCELAMENTO",
                        observacao = "Criado pela importacao Nubank CSV a partir da parcela ${ultimaParcelaImportada}/${totalParcelas}",
                        idLote = grupo.first().loteId,
                        mesInicio = mesInicio,
                        mesFim = mesFim,
                        diaPreferencial = dataReferencia.dayOfMonth,
                        parcelaInicio = ultimaParcelaImportada + 1,
                        parcelaTotal = totalParcelas,
                        frequencia = Frequencia.MENSAL,
                        status = StatusParcelamento.ATIVO
                    )
                )
            }
    }

    private fun buscarParcelamentoAtivoCorrespondente(linha: LinhaConfirmadaParcelamento) =
        recorrenciaRepository.findByCategoriaAndValorCentavos("PARCELAMENTO", linha.valorCentavos)
            .filter { it.status == StatusParcelamento.ATIVO }
            .firstOrNull { normalizarChaveCompra(it.descricao) == normalizarChaveCompra(linha.descricaoLimpa) }

    private fun removerMaterializacoesQuitadas(recorrenciaId: Long?, mesQuitacao: YearMonth, proximoMesPendente: YearMonth) {
        if (recorrenciaId == null || !proximoMesPendente.isAfter(mesQuitacao)) return

        lancamentoRepository.deleteMaterializadosDaRecorrenciaNoIntervalo(
            recorrenciaId = recorrenciaId,
            mesInicio = mesQuitacao.toString(),
            mesFimExclusivo = proximoMesPendente.toString(),
        )
    }

    private fun observacaoComAntecipacao(
        grupo: List<LinhaConfirmadaParcelamento>,
        primeiraParcelaImportada: Int,
        ultimaParcelaImportada: Int,
        totalParcelas: Int,
    ): String {
        val prefixo = if (grupo.mapNotNull { it.parcelaAtual }.distinct().size > 1) {
            "Antecipacao detectada"
        } else {
            "Atualizado pela importacao Nubank CSV"
        }
        return "$prefixo a partir das parcelas ${primeiraParcelaImportada}-${ultimaParcelaImportada}/${totalParcelas}"
    }

    private fun normalizarChaveCompra(descricao: String): String {
        val semAcentos = Normalizer.normalize(descricao.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return semAcentos.replace("[^a-z0-9]".toRegex(), "")
    }

    private data class LinhaConfirmadaParcelamento(
        val descricaoLimpa: String,
        val valorCentavos: Long,
        val data: LocalDate,
        val loteId: String,
        val parcelaAtual: Int?,
        val parcelaTotal: Int?,
    )
}
