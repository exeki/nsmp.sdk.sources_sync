package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import ru.kazantsev.nsmp.source_sync.cli.AbstractCommand
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
        assertTrue(result.stderr.contains(AbstractCommand.CONNECTOR_MODES_VALIDATION_MSG))
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
        assertTrue(result.stderr.contains("${AbstractCommand.DIRECT_CONNECTOR_MODE_REQUIRED_MSG} --accessKey"))
    }

    @Test
    fun checkConnectorOverridesWithoutInstallationIdExecution() {
        val result = runCommand(
            "pull",
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.CONFIG_PATH.withDefaultValue()
        )

        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains(AbstractCommand.INSTALLATION_ID_REQUIRED_MSG))
    }
}
