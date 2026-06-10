package br.com.contastermometro.lancamentos.mapper

import br.com.contastermometro.lancamentos.dto.LancamentoRequest
import br.com.contastermometro.lancamentos.model.Lancamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

class LancamentosMapperImplTest {

    private val mapper = LancamentosMapperImpl()

    @Test
    fun `to should map entity to response correctly`() {
        val entity = Lancamento(
            id = 1L,
            tipo = TipoLancamento.ENTRADA,
            descricao = "Salario",
            valorCentavos = 123456,
            dataLancamento = "2026-06-10",
            mesReferencia = "2026-06",
            categoria = "Salario",
            observacao = "",
        )

        val resp = mapper.to(entity)

        assertEquals(1L, resp.id)
        assertEquals(TipoLancamento.ENTRADA, resp.tipo)
        assertEquals("Salario", resp.descricao)
        assertEquals(BigDecimal.valueOf(123456, 2), resp.valor)
        assertEquals(LocalDate.parse("2026-06-10"), resp.data)
        assertEquals("2026-06", resp.mesReferencia)
    }

    @Test
    fun `from should map request to entity correctly`() {
        val req = LancamentoRequest(
            tipo = TipoLancamento.GASTO_DIARIO,
            descricao = "Mercado",
            valor = BigDecimal("123.45"),
            data = LocalDate.parse("2026-06-10"),
            mesReferencia = "2026-06",
            categoria = "Alimentacao",
            observacao = null,
        )

        val entity = mapper.from(req)

        assertEquals(TipoLancamento.GASTO_DIARIO, entity.tipo)
        assertEquals("Mercado", entity.descricao)
        assertEquals(12345L, entity.valorCentavos)
        assertEquals("2026-06-10", entity.dataLancamento)
        assertEquals("2026-06", entity.mesReferencia)
        assertEquals("Alimentacao", entity.categoria)
    }
}

