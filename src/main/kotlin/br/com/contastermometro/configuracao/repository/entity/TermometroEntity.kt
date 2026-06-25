package br.com.contastermometro.configuracao.repository.entity

import br.com.contastermometro.configuracao.dto.EstrategiaTermometro
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "configuracao_termometro")
class TermometroEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "reserva_minima_intocavel", nullable = false, precision = 15, scale = 2)
    var reservaMinimaIntocavel: BigDecimal,

    @Column(name = "orcamento_diario_minimo", nullable = false, precision = 15, scale = 2)
    var orcamentoDiarioMinimo: BigDecimal,

    @Column(name = "comprometimento_maximo_renda", nullable = false, precision = 5, scale = 4)
    var comprometimentoMaximoRenda: BigDecimal,

    @Column(name = "margem_seguranca", nullable = false, precision = 5, scale = 4)
    var margemSeguranca: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var estrategia: EstrategiaTermometro
)