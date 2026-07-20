package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.model.Lancamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LancamentoRepository : JpaRepository<Lancamento, Long> {
    fun findAllByMesReferenciaAndStatusOrderByDataLancamentoAscIdAsc(
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>

    fun findAllByMesReferenciaBetweenAndStatusOrderByMesReferenciaAscDataLancamentoAscIdAsc(
        mesInicio: String,
        mesFim: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>

    fun findByIdLote(idLote: String): List<Lancamento>

    @Modifying
    @Query("DELETE FROM Lancamento l WHERE l.idLote = :idLote")
    fun deleteByIdLoteApenasNoBanco(idLote: String)

    @Modifying
    @Query("DELETE FROM Lancamento l WHERE l.recorrenciaId IN :recorrenciaIds")
    fun deleteByRecorrenciaIdIn(recorrenciaIds: Collection<Long>)

    @Modifying
    @Query("""
        DELETE FROM Lancamento l
        WHERE l.recorrenciaId = :recorrenciaId
          AND l.recorrenciaExcecao = false
          AND l.mesReferencia >= :mesInicio
          AND l.mesReferencia < :mesFimExclusivo
    """)
    fun deleteMaterializadosDaRecorrenciaNoIntervalo(
        @Param("recorrenciaId") recorrenciaId: Long,
        @Param("mesInicio") mesInicio: String,
        @Param("mesFimExclusivo") mesFimExclusivo: String,
    )

    fun existsByDescricaoAndValorCentavosAndDataLancamento(
        descricao: String,
        valorCentavos: Long,
        dataLancamento: String
    ): Boolean

    fun existsByRecorrenciaIdAndMesReferencia(recorrenciaId: Long, mesReferencia: String): Boolean

    fun findAllByRecorrenciaIdAndStatusAndRecorrenciaExcecaoFalseOrderByMesReferenciaAscIdAsc(
        recorrenciaId: Long,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>

    fun findAllByRecorrenciaIdAndMesReferenciaGreaterThanEqualAndStatusAndRecorrenciaExcecaoFalseOrderByMesReferenciaAscIdAsc(
        recorrenciaId: Long,
        mesReferencia: String,
        status: StatusLancamento = StatusLancamento.ATIVO,
    ): List<Lancamento>
}
