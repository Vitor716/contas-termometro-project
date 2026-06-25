package br.com.contastermometro.orcamento.dto

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "snapshots_termometro")
class SnapshotTermometroMensal(
    @Id
    var mesReferencia: String,

    @Enumerated(EnumType.STRING)
    var statusAtual: StatusTermometro,

    var gastoDiarioRestanteCentavos: BigDecimal,
    var totalInvestidoCentavos: BigDecimal,
    var performanceContraMetaBps: Int,

    @Version
    var version: Long? = null
)