package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.Frequencia
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import jakarta.persistence.*
import java.time.Instant
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "recorrencias_lancamento")
class RecorrenciaLancamento(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tipo: TipoLancamento,

    @Column(nullable = false)
    var descricao: String,

    @Column(name = "valor_centavos", nullable = false)
    var valorCentavos: Long,

    @Column(name = "mes_inicio", nullable = false, length = 7)
    var mesInicio: String,

    @Column(name = "mes_fim", length = 7)
    var mesFim: String? = null,

    @Column(name = "dia_preferencial", nullable = false)
    var diaPreferencial: Int,

    @Column(name = "parcela_inicio")
    var parcelaInicio: Int? = null,

    @Column(name = "parcela_total")
    var parcelaTotal: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var frequencia: Frequencia = Frequencia.MENSAL,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusParcelamento = StatusParcelamento.ATIVO,

    var categoria: String? = null,

    var observacao: String? = null,

    @Column(name = "id_lote", length = 100)
    var idLote: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: String = agora(),

    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: String = agora()
) {
    @PrePersist
    fun antesDeCriar() {
        val agora = agora()
        criadoEm = agora
        atualizadoEm = agora
    }

    @PreUpdate
    fun antesDeAtualizar() {
        atualizadoEm = agora()
    }

    fun estaVigenteNoMes(mesReferencia: String): Boolean {
        if (mesReferencia < this.mesInicio) return false

        if (this.mesFim != null && mesReferencia > this.mesFim!!) return false

        return this.status == StatusParcelamento.ATIVO
    }

    companion object {
        private fun agora(): String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    }
}
