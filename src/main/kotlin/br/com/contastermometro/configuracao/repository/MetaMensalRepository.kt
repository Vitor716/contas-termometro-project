package br.com.contastermometro.configuracao.repository

import br.com.contastermometro.configuracao.dto.MetaMensal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetaMensalRepository : JpaRepository<MetaMensal, Long>{
    fun findByMesReferencia(mes: String): MetaMensal?
}