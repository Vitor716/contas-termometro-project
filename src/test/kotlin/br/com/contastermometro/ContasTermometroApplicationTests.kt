package br.com.contastermometro

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:sqlite:file:contas_termometro_context_test?mode=memory&cache=shared",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
    ],
)
class ContasTermometroApplicationTests {
    @Test
    fun contextLoads() {
    }
}
