package br.com.contastermometro.configuracao.repository

import br.com.contastermometro.configuracao.repository.entity.MetaMensalEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetaMensalRepository : JpaRepository<MetaMensalEntity, Long>{
    fun findByMesReferencia(mes: String): MetaMensalEntity?
}