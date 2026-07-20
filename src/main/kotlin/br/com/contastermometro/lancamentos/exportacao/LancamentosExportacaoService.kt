package br.com.contastermometro.lancamentos.exportacao

import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.service.MaterializadorRecorrenciaService
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.YearMonth

@Service
class LancamentosExportacaoService(
    private val repository: LancamentoRepository,
    private val materializadorRecorrenciaService: MaterializadorRecorrenciaService,
) {
    fun exportarCsv(mesReferencia: YearMonth, escopo: EscopoExportacaoLancamentos): ArquivoCsvLancamentos {
        val mesInicio = mesReferencia.minusMonths(escopo.quantidadeMeses - 1)

        gerarSequenciaMensal(mesInicio, mesReferencia).forEach {
            materializadorRecorrenciaService.garantirRecorrenciasMaterializadasNoMes(it.toString())
        }

        val lancamentos = repository.findAllByMesReferenciaBetweenAndStatusOrderByMesReferenciaAscDataLancamentoAscIdAsc(
            mesInicio = mesInicio.toString(),
            mesFim = mesReferencia.toString(),
        )

        val csv = LancamentosCsvWriter.escrever(lancamentos)
        val nomeArquivo = "lancamentos-${mesInicio}-${mesReferencia}.csv"

        return ArquivoCsvLancamentos(
            nomeArquivo = nomeArquivo,
            conteudo = csv.toByteArray(StandardCharsets.UTF_8),
        )
    }

    private fun gerarSequenciaMensal(inicio: YearMonth, fim: YearMonth): Sequence<YearMonth> {
        return generateSequence(inicio) { atual ->
            atual.plusMonths(1).takeIf { !it.isAfter(fim) }
        }
    }
}
