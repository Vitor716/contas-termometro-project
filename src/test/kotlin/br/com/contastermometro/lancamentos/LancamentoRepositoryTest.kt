package br.com.contastermometro.lancamentos

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:sqlite:file:lancamentos_repository_test?mode=memory&cache=shared",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
    ],
)
class LancamentoRepositoryTest {

    @Autowired
    private lateinit var repository: LancamentoRepository

    @Test
    fun `persiste e lista lancamentos ativos do mes em ordem cronologica`() {
        repository.save(
            Lancamento(
                tipo = TipoLancamento.GASTO_DIARIO,
                descricao = "Mercado",
                valorCentavos = 12345,
                dataLancamento = "2026-06-10",
                mesReferencia = "2026-06",
                categoria = "Alimentacao",
            ),
        )
        repository.save(
            Lancamento(
                tipo = TipoLancamento.ENTRADA,
                descricao = "Salario",
                valorCentavos = 500000,
                dataLancamento = "2026-06-05",
                mesReferencia = "2026-06",
            ),
        )
        repository.save(
            Lancamento(
                tipo = TipoLancamento.GASTO_DIARIO,
                descricao = "Compra cancelada",
                valorCentavos = 5000,
                dataLancamento = "2026-06-01",
                mesReferencia = "2026-06",
                status = StatusLancamento.CANCELADO,
            ),
        )

        val lancamentos = repository.findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc("2026-06")

        assertThat(lancamentos).extracting<String> { it.descricao }
            .containsExactly("Salario", "Mercado")
    }
}
