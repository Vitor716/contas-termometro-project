package br.com.contastermometro.sistema

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/sistema")
class SistemaController(
    private val environment: Environment,
) {
    @GetMapping("/saude")
    fun saude(): RespostaSaudeSistema =
        RespostaSaudeSistema(
            status = "UP",
            application = environment.getProperty("spring.application.name", "contas-termometro-project"),
            environment = environment.getProperty("app.environment", "local"),
            checkedAt = Instant.now(),
        )
}

data class RespostaSaudeSistema(
    val status: String,
    val application: String,
    val environment: String,
    val checkedAt: Instant,
)
