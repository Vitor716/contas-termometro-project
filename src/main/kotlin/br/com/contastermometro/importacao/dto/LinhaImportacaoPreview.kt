package br.com.contastermometro.importacao.dto

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "linhas_importacao_preview")
class LinhaImportacaoPreview(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "lote_id", nullable = false)
    var loteId: String = "",

    @Column(name = "descricao_original", nullable = false)
    var descricaoOriginal: String = "",

    @Column(name = "descricao_limpa", nullable = false)
    var descricaoLimpa: String = "",

    @Column(name = "valor_centavos", nullable = false)
    var valorCentavos: Long = 0,

    @Column(name = "data", nullable = false)
    var data: String = "",

    @Column(name = "hash_linha", nullable = false)
    var hashLinha: String = "",

    @Column(name = "categoria_sugerida")
    var categoriaSugerida: String? = null,

    @Column(name = "is_parcelamento", nullable = false)
    var isParcelamento: Boolean = false,

    @Column(name = "parcela_atual")
    var parcelaAtual: Int? = null,

    @Column(name = "parcela_total")
    var parcelaTotal: Int? = null,

    @Column(name = "is_duplicidade", nullable = false)
    var isDuplicidade: Boolean = false,
)

data class LinhaImportacaoPreviewResponse(
    val id: Long,
    val descricaoOriginal: String,
    val descricaoLimpa: String,
    val valor: BigDecimal,
    val valorCentavos: Long,
    val data: LocalDate,
    val hashLinha: String,
    val categoriaSugerida: String?,
    val isParcelamento: Boolean,
    val parcelaAtual: Int?,
    val parcelaTotal: Int?,
    val isDuplicidade: Boolean,
)

fun LinhaImportacaoPreview.toResponse(): LinhaImportacaoPreviewResponse {
    return LinhaImportacaoPreviewResponse(
        id = id ?: 0,
        descricaoOriginal = descricaoOriginal,
        descricaoLimpa = descricaoLimpa,
        valor = BigDecimal(valorCentavos).movePointLeft(2),
        valorCentavos = valorCentavos,
        data = LocalDate.parse(data),
        hashLinha = hashLinha,
        categoriaSugerida = categoriaSugerida,
        isParcelamento = isParcelamento,
        parcelaAtual = parcelaAtual,
        parcelaTotal = parcelaTotal,
        isDuplicidade = isDuplicidade,
    )
}
