package br.com.contastermometro.configuracao.dto

import jakarta.persistence.*
import java.time.Instant
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "metas_mensais")
class MetaMensal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "mes_referencia", nullable = false, length = 7)
    var mesReferencia: String,

    @Column(name = "percentual_meta_investimento_bps", nullable = false)
    var percentualMetaInvestimentoBps: Int,

    @Column(name = "orcamento_diario_minimo_centavos", nullable = false)
    var orcamentoDiarioMinimoCentavos: Long = 0,

    @Column(length = 255)
    var motivo: String? = null,

    @Column(name = "vigente_desde", nullable = false)
    var vigenteDesde: String = agora(),

    @Column(name = "vigente_ate")
    var vigenteAte: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: String = agora()
) {
    companion object {
        private fun agora(): String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    }
}