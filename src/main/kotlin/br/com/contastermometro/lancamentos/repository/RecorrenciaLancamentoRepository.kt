package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.dto.RecorrenciaLancamento
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RecorrenciaLancamentoRepository : JpaRepository<RecorrenciaLancamento, Long>{

    @Query("""
        SELECT r FROM RecorrenciaLancamento r 
        WHERE r.status = 'ATIVO' 
        AND r.mesInicio <= :mesReferencia 
        AND (r.mesFim IS NULL OR r.mesFim >= :mesReferencia)
    """)
    fun findVigentesParaOMes(mesReferencia: String): List<RecorrenciaLancamento>

    fun findByCategoria(categoria: String): List<RecorrenciaLancamento>

    fun findByCategoriaAndValorCentavos(categoria: String, valorCentavos: Long): List<RecorrenciaLancamento>

    @Query("""
        SELECT r FROM RecorrenciaLancamento r
        WHERE r.mesInicio <= :mes 
          AND (r.mesFim IS NULL OR r.mesFim >= :mes)
          AND r.status = :status
        ORDER BY r.id ASC
    """)
    fun findAtivasPorMes(
        @Param("mes") mes: String,
        @Param("status") status: StatusParcelamento
    ): List<RecorrenciaLancamento>

    fun findByIdLote(idLote: String): List<RecorrenciaLancamento>

    @Modifying
    @Query("DELETE FROM RecorrenciaLancamento r WHERE r.idLote = :idLote")
    fun deleteByIdLote(idLote: String)
}
