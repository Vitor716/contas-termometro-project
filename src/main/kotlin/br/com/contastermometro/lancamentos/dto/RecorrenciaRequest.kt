package br.com.contastermometro.lancamentos.dto

import br.com.contastermometro.lancamentos.enums.Frequencia
import br.com.contastermometro.lancamentos.enums.StatusParcelamento
import br.com.contastermometro.lancamentos.enums.TipoLancamento

data class RecorrenciaRequest(
    val tipo: TipoLancamento,
    val descricao: String,
    val valorCentavos: Long,
    val categoria: String? = null,
    val observacao: String? = null,
    val mesInicio: String,
    val mesFim: String? = null,
    val diaPreferencial: Int,
    val frequencia: Frequencia,
    val status: StatusParcelamento
)