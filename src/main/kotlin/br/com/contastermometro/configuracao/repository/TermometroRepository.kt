package br.com.contastermometro.configuracao.repository

import br.com.contastermometro.configuracao.repository.entity.TermometroEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermometroRepository : JpaRepository<TermometroEntity, Long>{
}