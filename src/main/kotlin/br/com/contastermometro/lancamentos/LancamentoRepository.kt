package br.com.contastermometro.lancamentos

import org.springframework.data.jpa.repository.JpaRepository

interface LancamentoRepository : JpaRepository<Lancamento, Long> {
    fun findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>
}
