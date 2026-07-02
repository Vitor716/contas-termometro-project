package br.com.contastermometro.shared.handler

import br.com.contastermometro.backup.BackupException
import br.com.contastermometro.shared.LancamentoNaoEncontradoException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(
        LancamentoNaoEncontradoException::class,
        MethodArgumentNotValidException::class

    )
    fun handleLancamentoNaoEncontradoException(
        ex: LancamentoNaoEncontradoException,
        request: HttpServletRequest
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Recurso não encontrado."
        ).apply {
            title = "Lançamento não encontrado"
            instance = URI.create(request.requestURI)
        }

        return problemDetail
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMesInvalidoException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ProblemDetail {
        val isErroNoMes = ex.propertyName == "mes"

        val mensagemAmigavel = if (isErroNoMes) {
            "Formato de mês inválido. Use o padrão 'yyyy-MM'."
        } else {
            "Parâmetro inválido na requisição."
        }

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            mensagemAmigavel
        ).apply {
            title = "Parâmetro Inválido"
            instance = URI.create(request.requestURI)
        }

        return problemDetail
    }

    @ExceptionHandler(BackupException::class)
    fun handleBackupException(
        ex: BackupException,
        request: HttpServletRequest
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Nao foi possivel processar o backup."
        ).apply {
            title = "Backup invalido"
            instance = URI.create(request.requestURI)
        }
}
