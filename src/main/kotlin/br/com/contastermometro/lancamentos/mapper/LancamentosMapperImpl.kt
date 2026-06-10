package br.com.contastermometro.lancamentos.mapper

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.dto.LancamentoResponse
import br.com.contastermometro.lancamentos.model.Lancamento
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class LancamentosMapperImpl : LancamentosMapper {

    override fun to(model: Lancamento): LancamentoResponse {
        val valor = BigDecimal.valueOf(model.valorCentavos, 2)
        val data = LocalDate.parse(model.dataLancamento)

        return LancamentoResponse(
            id = model.id!!,
            tipo = model.tipo,
            descricao = model.descricao,
            valor = valor,
            data = data,
            mesReferencia = model.mesReferencia,
            categoria = model.categoria,
            observacao = model.observacao,
        )
    }
    
    override fun from(request: LancamentoRequest): Lancamento {
        val centavos = request.valor.movePointRight(2).longValueExact()
        val data = request.data.toString()

        return Lancamento(
            id = null,
            tipo = request.tipo,
            descricao = request.descricao,
            valorCentavos = centavos,
            dataLancamento = data,
            mesReferencia = request.mesReferencia,
            categoria = request.categoria,
            observacao = request.observacao,
        )
    }

}
