package br.com.contastermometro.orcamento.service.impl

import br.com.contastermometro.orcamento.dto.SnapshotTermometroMensal
import br.com.contastermometro.orcamento.repository.SnapshotTermometroRepository
import br.com.contastermometro.orcamento.service.SnapshotTermometroService
import org.springframework.stereotype.Service

@Service
class SnapshotTermometroServiceImpl (
    private val snapshotTermometroRepository: SnapshotTermometroRepository
) : SnapshotTermometroService {

    override fun buscarPorId(mes: String) : SnapshotTermometroMensal {
        return snapshotTermometroRepository.findById(mes)
            .orElseThrow { IllegalArgumentException("Snapshot não encontrado para o mês $mes.") }
    }
}