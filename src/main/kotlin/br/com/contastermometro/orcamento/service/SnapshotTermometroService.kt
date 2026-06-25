package br.com.contastermometro.orcamento.service

import br.com.contastermometro.orcamento.dto.SnapshotTermometroMensal

interface SnapshotTermometroService {
    fun buscarPorId(mes: String) : SnapshotTermometroMensal
}