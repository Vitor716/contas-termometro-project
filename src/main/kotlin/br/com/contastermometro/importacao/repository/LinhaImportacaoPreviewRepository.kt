package br.com.contastermometro.importacao.repository

import br.com.contastermometro.importacao.dto.LinhaImportacaoPreview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface LinhaImportacaoPreviewRepository : JpaRepository<LinhaImportacaoPreview, Long> {
    fun findByLoteId(loteId: String): List<LinhaImportacaoPreview>

    @Modifying
    fun deleteByLoteId(loteId: String)
}
