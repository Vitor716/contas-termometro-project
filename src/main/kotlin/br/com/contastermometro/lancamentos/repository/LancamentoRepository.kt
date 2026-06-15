package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import org.springframework.data.jpa.repository.JpaRepository

interface LancamentoRepository : JpaRepository<Lancamento, Long> {
    fun findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>

    fun findByMesReferencia(mesReferencia: String): List<Lancamento>
}