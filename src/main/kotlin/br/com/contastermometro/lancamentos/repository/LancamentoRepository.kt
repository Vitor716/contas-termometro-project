package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface LancamentoRepository : JpaRepository<Lancamento, Long> {
    fun findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>

    fun findByIdLote(idLote: String): List<Lancamento>

    @Modifying
    @Query("DELETE FROM Lancamento l WHERE l.idLote = :idLote")
    fun deleteByIdLoteApenasNoBanco(idLote: String)

    fun existsByRecorrenciaIdAndMesReferencia(recorrenciaId: Long, mesReferencia: String): Boolean
}