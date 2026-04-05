package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncCheckCommandFunctionalTest : CommandFunctionalTestBase(), ICommandTest {
    override val commandName: String = "syncCheck"

    @Test
    override fun checkExists() {
        val result = runCommand(commandName)
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("SMP installation identifier is not configured"))
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *directConnectorArgs()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *connectorArgsByConfigFileInPath()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkEmptyExecution() {
        val result = runCommand(commandName, *connectorArgsByConfigFile())
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("No sources found to sync check"))
    }

    @Test
    override fun checkScriptsExecution() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkModulesExecution() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkFullExecution() {
        writeLocalInfoFile()
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }
}
