package br.com.contastermometro.lancamentos.exportacao

enum class EscopoExportacaoLancamentos(val quantidadeMeses: Long) {
    MES(1),
    TRES_MESES(3),
    SEIS_MESES(6),
    UM_ANO(12),
}
