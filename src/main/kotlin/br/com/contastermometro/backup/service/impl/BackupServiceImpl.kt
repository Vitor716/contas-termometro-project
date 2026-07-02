package br.com.contastermometro.backup.service.impl

import br.com.contastermometro.backup.BackupException
import br.com.contastermometro.backup.dto.BackupInfoResponse
import br.com.contastermometro.backup.dto.BackupRestoreResponse
import br.com.contastermometro.backup.service.BackupArquivo
import br.com.contastermometro.backup.service.BackupService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Service
class BackupServiceImpl(
    private val objectMapper: ObjectMapper,
    @Value("\${spring.datasource.url}") private val datasourceUrl: String,
) : BackupService {

    private val secureRandom = SecureRandom()
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)

    override fun info(): BackupInfoResponse {
        val banco = databasePath()
        return BackupInfoResponse(
            caminhoBanco = banco.toAbsolutePath().normalize().toString(),
            bancoExiste = Files.exists(banco),
            tamanhoBytes = if (Files.exists(banco)) Files.size(banco) else 0,
            atualizadoEm = if (Files.exists(banco)) Files.getLastModifiedTime(banco).toInstant() else null,
            schemaVersion = currentSchemaVersion(),
            formatoBackupAtual = FORMATO_BACKUP,
            diretorioBackupsAutomaticos = automaticBackupDir().toAbsolutePath().normalize().toString(),
        )
    }

    override fun exportar(senha: String): BackupArquivo {
        val tempDir = Files.createTempDirectory("contas-backup-export-")
        try {
            val dbSnapshot = tempDir.resolve("contas-termometro.db")
            vacuumInto(dbSnapshot)

            val manifesto = BackupManifesto(
                formato = FORMATO_BACKUP,
                app = "contas-termometro",
                criadoEm = Instant.now().toString(),
                schemaVersion = currentSchemaVersion(),
                sqliteSha256 = sha256Hex(dbSnapshot),
            )

            val zip = zipPayload(dbSnapshot, manifesto)
            val encrypted = encrypt(zip, senha)
            val nome = "contas-termometro-${formatter.format(Instant.now())}.ctbackup"
            return BackupArquivo(nome, encrypted)
        } catch (ex: BackupException) {
            throw ex
        } catch (ex: Exception) {
            throw BackupException("Nao foi possivel exportar o backup.", ex)
        } finally {
            tempDir.deleteRecursivelyQuietly()
        }
    }

    override fun restaurar(file: MultipartFile, senha: String): BackupRestoreResponse {
        if (file.isEmpty) throw BackupException("Selecione um arquivo de backup.")

        val tempDir = Files.createTempDirectory("contas-backup-restore-")
        try {
            val payload = decrypt(file.bytes, senha)
            val extracted = unzipPayload(payload, tempDir)
            val currentVersion = currentSchemaVersion()
            if (extracted.manifesto.formato != FORMATO_BACKUP) {
                throw BackupException("Formato de backup incompatível.")
            }
            if (extracted.manifesto.schemaVersion != currentVersion) {
                throw BackupException(
                    "Backup bloqueado: versao do banco ${extracted.manifesto.schemaVersion}, aplicacao atual $currentVersion."
                )
            }
            if (sha256Hex(extracted.database) != extracted.manifesto.sqliteSha256) {
                throw BackupException("Backup corrompido: checksum do banco nao confere.")
            }
            validarBancoBackup(extracted.database)

            val automatico = exportarBackupAutomaticoAntesDaRestauracao()
            val tabelas = restaurarTabelas(extracted.database)

            return BackupRestoreResponse(
                mensagem = "Backup restaurado com sucesso.",
                tabelasRestauradas = tabelas,
                backupAutomatico = automatico.toAbsolutePath().normalize().toString(),
            )
        } catch (ex: BackupException) {
            throw ex
        } catch (ex: Exception) {
            throw BackupException("Nao foi possivel restaurar o backup: ${ex.message}", ex)
        } finally {
            tempDir.deleteRecursivelyQuietly()
        }
    }

    private fun databasePath(): Path {
        val prefix = "jdbc:sqlite:"
        if (!datasourceUrl.startsWith(prefix)) {
            throw BackupException("Backup local suporta apenas datasource SQLite.")
        }
        val rawPath = datasourceUrl.removePrefix(prefix).substringBefore("?")
        if (rawPath.isBlank() || rawPath == ":memory:") {
            throw BackupException("Banco SQLite em memoria nao pode ser exportado.")
        }
        val path = Path.of(rawPath)
        return if (path.isAbsolute) path else Path.of("").toAbsolutePath().resolve(path).normalize()
    }

    private fun currentSchemaVersion(): String =
        currentConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "select version from flyway_schema_history where success = 1 order by installed_rank desc limit 1"
                ).use { rs ->
                    if (rs.next()) rs.getString(1) else "0"
                }
            }
        }

    private fun vacuumInto(target: Path) {
        Files.createDirectories(target.parent)
        currentConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("VACUUM INTO '${target.toAbsolutePath().normalize().toString().replace("'", "''")}'")
            }
        }
    }

    private fun zipPayload(database: Path, manifesto: BackupManifesto): ByteArray =
        ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(objectMapper.writeValueAsBytes(manifesto))
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("contas-termometro.db"))
                Files.copy(database, zip)
                zip.closeEntry()
            }
            output.toByteArray()
        }

    private fun unzipPayload(payload: ByteArray, tempDir: Path): BackupExtraido {
        var manifesto: BackupManifesto? = null
        var database: Path? = null

        ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                when (entry.name) {
                    "manifest.json" -> manifesto = objectMapper.readValue(zip.readBytes(), BackupManifesto::class.java)
                    "contas-termometro.db" -> {
                        val target = tempDir.resolve("restaurar.db")
                        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING)
                        database = target
                    }
                }
                zip.closeEntry()
            }
        }

        return BackupExtraido(
            manifesto ?: throw BackupException("Backup sem manifesto."),
            database ?: throw BackupException("Backup sem banco SQLite."),
        )
    }

    private fun validarBancoBackup(database: Path) {
        DriverManager.getConnection("jdbc:sqlite:${database.toAbsolutePath().normalize()}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA integrity_check").use { rs ->
                    if (!rs.next() || rs.getString(1) != "ok") {
                        throw BackupException("Backup invalido: falha no integrity_check do SQLite.")
                    }
                }
                statement.executeQuery("select count(*) from flyway_schema_history where success = 1").use { rs ->
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw BackupException("Backup invalido: historico de migrations ausente.")
                    }
                }
            }
        }
    }

    private fun exportarBackupAutomaticoAntesDaRestauracao(): Path {
        val dir = automaticBackupDir()
        Files.createDirectories(dir)
        val target = dir.resolve("antes-restauracao-${formatter.format(Instant.now())}.db")
        vacuumInto(target)
        return target
    }

    private fun automaticBackupDir(): Path =
        databasePath().parent?.resolve("backups-automaticos")
            ?: Path.of("").toAbsolutePath().resolve("backups-automaticos")

    private fun restaurarTabelas(sourceDatabase: Path): Int =
        tabelasRestauraveis(sourceDatabase).let { tabelas ->
            currentConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = OFF")
            }
            connection.autoCommit = false
            try {
                connection.createStatement().use { statement ->
                    statement.execute("ATTACH DATABASE '${sourceDatabase.toAbsolutePath().normalize().toString().replace("'", "''")}' AS backup")
                    tabelas.asReversed().forEach { tabela ->
                        statement.executeUpdate("DELETE FROM main.${quote(tabela.nome)}")
                    }
                    tabelas.forEach { tabela ->
                        val cols = tabela.colunas.joinToString(", ") { quote(it) }
                        statement.executeUpdate(
                            "INSERT INTO main.${quote(tabela.nome)} ($cols) SELECT $cols FROM backup.${quote(tabela.nome)}"
                        )
                    }
                    statement.execute("PRAGMA foreign_keys = ON")
                }
                connection.commit()
                tabelas.size
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            } finally {
                connection.autoCommit = true
                connection.createStatement().use { statement ->
                    statement.execute("PRAGMA foreign_keys = ON")
                }
            }
        }
        }

    private fun tabelasRestauraveis(sourceDatabase: Path): List<TabelaBackup> {
        val targetTables = currentConnection().use { target ->
            tableColumns(target)
        }
        val sourceTables = DriverManager.getConnection("jdbc:sqlite:${sourceDatabase.toAbsolutePath().normalize()}").use { source ->
            tableColumns(source)
        }

        if (targetTables.keys != sourceTables.keys) {
            throw BackupException("Backup incompativel: conjunto de tabelas diferente da aplicacao atual.")
        }
        targetTables.forEach { (table, columns) ->
            if (columns != sourceTables[table]) {
                throw BackupException("Backup incompativel: colunas da tabela $table nao conferem.")
            }
        }
        return targetTables.map { (table, columns) -> TabelaBackup(table, columns) }
    }

    private fun currentConnection(): Connection =
        DriverManager.getConnection(datasourceUrl)

    private fun tableColumns(connection: Connection): LinkedHashMap<String, List<String>> =
        linkedMapOf<String, List<String>>().also { result ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    """
                    select name
                    from sqlite_master
                    where type = 'table'
                      and name not like 'sqlite_%'
                      and name <> 'flyway_schema_history'
                    order by name
                    """.trimIndent()
                ).use { rs ->
                    while (rs.next()) {
                        val table = rs.getString("name")
                        result[table] = columns(connection, table)
                    }
                }
            }
        }

    private fun columns(connection: Connection, table: String): List<String> =
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA table_info(${quote(table)})").use { rs ->
                val cols = mutableListOf<String>()
                while (rs.next()) cols += rs.getString("name")
                cols
            }
        }

    private fun encrypt(plain: ByteArray, senha: String): ByteArray {
        val salt = randomBytes(SALT_BYTES)
        val iv = randomBytes(IV_BYTES)
        val key = deriveKey(senha, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(MAGIC.size + 1 + salt.size + iv.size + encrypted.size)
            .put(MAGIC)
            .put(1)
            .put(salt)
            .put(iv)
            .put(encrypted)
            .array()
    }

    private fun decrypt(encrypted: ByteArray, senha: String): ByteArray {
        if (encrypted.size < MAGIC.size + 1 + SALT_BYTES + IV_BYTES + 1) {
            throw BackupException("Arquivo de backup invalido.")
        }
        val buffer = ByteBuffer.wrap(encrypted)
        val magic = ByteArray(MAGIC.size)
        buffer.get(magic)
        if (!magic.contentEquals(MAGIC)) throw BackupException("Arquivo de backup invalido.")
        val cryptoVersion = buffer.get().toInt()
        if (cryptoVersion != 1) throw BackupException("Criptografia do backup incompativel.")
        val salt = ByteArray(SALT_BYTES)
        val iv = ByteArray(IV_BYTES)
        buffer.get(salt)
        buffer.get(iv)
        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(senha, salt), GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.doFinal(cipherText)
        } catch (ex: Exception) {
            throw BackupException("Senha incorreta ou backup corrompido.", ex)
        }
    }

    private fun deriveKey(senha: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(senha.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { secureRandom.nextBytes(it) }

    private fun sha256Hex(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun quote(identifier: String): String = "\"${identifier.replace("\"", "\"\"")}\""

    private fun Path.deleteRecursivelyQuietly() {
        runCatching {
            if (Files.exists(this)) {
                Files.walk(this)
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.deleteIfExists(it) }
            }
        }
    }

    private data class BackupManifesto(
        val formato: Int,
        val app: String,
        val criadoEm: String,
        val schemaVersion: String,
        val sqliteSha256: String,
    )

    private data class BackupExtraido(
        val manifesto: BackupManifesto,
        val database: Path,
    )

    private data class TabelaBackup(
        val nome: String,
        val colunas: List<String>,
    )

    companion object {
        private const val FORMATO_BACKUP = 1
        private val MAGIC = byteArrayOf(0x43, 0x54, 0x42, 0x4B, 0x50, 0x31) // CTBKP1
        private const val SALT_BYTES = 16
        private const val IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
        private const val KEY_BITS = 256
        private const val PBKDF2_ITERATIONS = 210_000
    }
}
