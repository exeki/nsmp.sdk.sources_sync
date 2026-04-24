package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConnectorParamsValidationFunctionalTest : CommandFunctionalTestBase() {

    @Test
    fun checkMixedConnectorModesExecution() {
        val result = runCommand(
            "pull",
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.INSTALLATION_ID.withDefaultValue(),
            CommandArgs.CONFIG_PATH.withDefaultValue(),
            CommandArgs.SCHEME.withDefaultValue(),
            CommandArgs.HOST.withDefaultValue(),
            CommandArgs.ACCESS_KEY.withDefaultValue()
        )

        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("Connector options must use either config file mode"))
    }

    @Test
    fun checkIncompleteDirectConnectorModeExecution() {
        val result = runCommand(
            "pull",
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.INSTALLATION_ID.withDefaultValue(),
            CommandArgs.SCHEME.withDefaultValue(),
            CommandArgs.HOST.withDefaultValue()
        )

        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("Direct connector mode requires --accessKey"))
    }

    @Test
    fun checkConnectorOverridesWithoutInstallationIdExecution() {
        val result = runCommand(
            "pull",
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.CONFIG_PATH.withDefaultValue()
        )

        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("Option --installationId is required"))
    }
}
