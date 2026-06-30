package br.com.contastermometro.lancamentos.service.impl

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.dto.toResponse
import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import br.com.contastermometro.lancamentos.repository.LancamentoRepository
import br.com.contastermometro.lancamentos.repository.RecorrenciaLancamentoRepository
import br.com.contastermometro.lancamentos.service.MaterializadorRecorrenciaService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class MaterializadorRecorrenciaServiceImpl (
    private val recorrenciaRepository: RecorrenciaLancamentoRepository,
    private val lancamentoRepository: LancamentoRepository,
) : MaterializadorRecorrenciaService {

    @Transactional
    override fun garantirRecorrenciasMaterializadasNoMes(mesReferencia: String) {
        val recorrenciasVigentes = recorrenciaRepository.findVigentesParaOMes(mesReferencia)

        recorrenciasVigentes.forEach { recorrencia ->
            val jaMaterializado = lancamentoRepository.existsByRecorrenciaIdAndMesReferencia(
                recorrencia.id!!,
                mesReferencia
            )

            if (!jaMaterializado) {
                val dataLancamentoString = montarData(mesReferencia, recorrencia.diaPreferencial)

                val novoLancamento = Lancamento(
                    tipo = recorrencia.tipo,
                    descricao = recorrencia.descricao,
                    valorCentavos = recorrencia.valorCentavos,
                    dataLancamento = dataLancamentoString,
                    mesReferencia = mesReferencia,
                    categoria = recorrencia.categoria,
                    observacao = "Materializado automaticamente a partir da regra #${recorrencia.id}",
                    status = StatusLancamento.ATIVO,
                    recorrenciaId = recorrencia.id,
                    recorrenciaExcecao = false
                )

                lancamentoRepository.save(novoLancamento)
            }
        }
    }

    override fun editarComoExcecao(original: Lancamento, req: LancamentoRequest): LancamentoResponse {
        original.status = StatusLancamento.CANCELADO
        lancamentoRepository.save(original)

        val excecao = Lancamento(
            tipo = original.tipo,
            descricao = req.descricao,
            valorCentavos = req.valor.movePointRight(2).toLong(),
            dataLancamento = req.data.toString(),
            mesReferencia = req.mesReferencia,
            recorrenciaId = original.recorrenciaId,
            recorrenciaExcecao = true
        )

        return lancamentoRepository.save(excecao).toResponse()
    }

    private fun montarData(mes: String, dia: Int): String {
        val yearMonth = YearMonth.parse(mes)

        val diaSeguro = if (dia > yearMonth.lengthOfMonth()) {
            yearMonth.lengthOfMonth()
        } else {
            dia
        }

        val dataCompleta = yearMonth.atDay(diaSeguro)

        return dataCompleta.toString()
    }
}