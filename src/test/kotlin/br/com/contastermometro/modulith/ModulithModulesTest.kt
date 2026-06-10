package br.com.contastermometro.modulith

import br.com.contastermometro.ContasTermometroApplication
import br.com.contastermometro.modulith.fixture.ModulithFixtureApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModulithModulesTest {

    @Test
    fun `application modules respect modulith boundaries`() {
        val modules = ApplicationModules.of(ContasTermometroApplication::class.java)

        val violations = modules.detectViolations()

        assertThat(violations.hasViolations()).isFalse
    }

    @Test
    fun `modulith detects dependency on another module internal package`() {
        val modules = ApplicationModules.of(ModulithFixtureApplication::class.java)

        val violations = modules.detectViolations()

        assertThat(violations.hasViolations()).isTrue
        assertThat(violations.toString()).contains("internal")
    }
}

