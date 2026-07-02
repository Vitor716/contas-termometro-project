package br.com.contastermometro.importacao.repository

import br.com.contastermometro.importacao.dto.LoteImportacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoteImportacaoRepository : JpaRepository<LoteImportacao, String> {
    fun existsByHashArquivo(hashArquivo: String): Boolean
}
