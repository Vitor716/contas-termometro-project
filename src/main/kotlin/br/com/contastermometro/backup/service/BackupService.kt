package br.com.contastermometro.backup.service

import br.com.contastermometro.backup.dto.BackupInfoResponse
import br.com.contastermometro.backup.dto.BackupRestoreResponse
import org.springframework.web.multipart.MultipartFile

interface BackupService {
    fun info(): BackupInfoResponse
    fun exportar(senha: String): BackupArquivo
    fun restaurar(file: MultipartFile, senha: String): BackupRestoreResponse
}

data class BackupArquivo(
    val nomeArquivo: String,
    val conteudo: ByteArray,
)
