package br.com.contastermometro.backup.dto

data class BackupRestoreResponse(
    val mensagem: String,
    val tabelasRestauradas: Int,
    val backupAutomatico: String,
)
