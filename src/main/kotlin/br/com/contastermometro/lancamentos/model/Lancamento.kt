package br.com.contastermometro.lancamentos.model

import br.com.contastermometro.lancamentos.enums.StatusLancamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "lancamentos")
class Lancamento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "id_lote", length = 100)
    var idLote: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var tipo: TipoLancamento = TipoLancamento.GASTO_DIARIO,

    @Column(nullable = false, length = 160)
    var descricao: String = "",

    @Column(name = "valor_centavos", nullable = false)
    var valorCentavos: Long = 0,

    @Column(name = "data_lancamento", nullable = false)
    var dataLancamento: String = "",

    @Column(name = "mes_referencia", nullable = false, length = 7)
    var mesReferencia: String = "",

    @Column(length = 80)
    var categoria: String? = null,

    @Column(length = 500)
    var observacao: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StatusLancamento = StatusLancamento.ATIVO,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: String = agora(),

    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: String = agora(),
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

    companion object {
        private fun agora(): String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    }
}