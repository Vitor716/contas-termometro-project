package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.model.Lancamento
import br.com.contastermometro.lancamentos.enums.StatusLancamento
import org.springframework.data.jpa.repository.JpaRepository

interface LancamentoRepository : JpaRepository<Lancamento, Long> {
    fun findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>
}