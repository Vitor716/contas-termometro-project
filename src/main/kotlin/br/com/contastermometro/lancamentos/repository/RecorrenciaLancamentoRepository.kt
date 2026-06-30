package br.com.contastermometro.lancamentos.repository

import br.com.contastermometro.lancamentos.dto.RecorrenciaLancamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RecorrenciaLancamentoRepository : JpaRepository<RecorrenciaLancamento, Long>{

    @Query("""
        SELECT r FROM RecorrenciaLancamento r 
        WHERE r.status = 'ATIVA' 
        AND r.mesInicio <= :mesReferencia 
        AND (r.mesFim IS NULL OR r.mesFim >= :mesReferencia)
    """)
    fun findVigentesParaOMes(mesReferencia: String): List<RecorrenciaLancamento>
}