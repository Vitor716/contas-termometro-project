package br.com.contastermometro.orcamento.repository

import br.com.contastermometro.orcamento.dto.SnapshotTermometroMensal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnapshotTermometroRepository : JpaRepository<SnapshotTermometroMensal, String> {
}