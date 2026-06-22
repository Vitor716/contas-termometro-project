package br.com.contastermometro.importacao.dto

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "lotes_importacao")
class LoteImportacao(
    @Id
    @Column(name = "id_lote")
    var idLote: String = "",

    @Column(nullable = false)
    var origem: String = "",

    @Column(name = "qtd_sucessos", nullable = false)
    var qtdSucessos: Int = 0,

    @Column(name = "qtd_falhas", nullable = false)
    var qtdFalhas: Int = 0,

    @Column(name = "total_processado", nullable = false)
    var totalProcessado: Int = 0,

    @Column(name = "log_falhas_json", columnDefinition = "TEXT")
    var logFalhasJson: String? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
)