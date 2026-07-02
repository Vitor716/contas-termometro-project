package br.com.contastermometro.backup.controller

import br.com.contastermometro.backup.dto.BackupInfoResponse
import br.com.contastermometro.backup.dto.BackupRestoreResponse
import br.com.contastermometro.backup.dto.ExportarBackupRequest
import br.com.contastermometro.backup.service.BackupService
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("/api/backups")
class BackupController(
    private val backupService: BackupService,
) {
    @GetMapping("/info")
    fun info(): BackupInfoResponse = backupService.info()

    @PostMapping("/exportar", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun exportar(@Valid @RequestBody request: ExportarBackupRequest): ResponseEntity<ByteArrayResource> {
        val arquivo = backupService.exportar(request.senha)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(arquivo.nomeArquivo).build().toString()
            )
            .body(ByteArrayResource(arquivo.conteudo))
    }

    @PostMapping("/restaurar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun restaurar(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("senha")
        @Size(min = 8, message = "A senha do backup precisa ter pelo menos 8 caracteres.")
        senha: String,
    ): BackupRestoreResponse = backupService.restaurar(file, senha)
}
