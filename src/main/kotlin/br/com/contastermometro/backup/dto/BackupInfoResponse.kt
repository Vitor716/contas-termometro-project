package br.com.contastermometro.backup.dto

import java.time.Instant

data class BackupInfoResponse(
    val caminhoBanco: String,
    val bancoExiste: Boolean,
    val tamanhoBytes: Long,
    val atualizadoEm: Instant?,
    val schemaVersion: String,
    val formatoBackupAtual: Int,
    val diretorioBackupsAutomaticos: String,
)
