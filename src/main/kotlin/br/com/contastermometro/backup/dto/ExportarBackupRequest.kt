package br.com.contastermometro.backup.dto

import jakarta.validation.constraints.Size

data class ExportarBackupRequest(
    @field:Size(min = 8, message = "A senha do backup precisa ter pelo menos 8 caracteres.")
    val senha: String,
)
