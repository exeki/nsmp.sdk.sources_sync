package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PullCommandFunctionalTest : CommandFunctionalTestBase(), ICommandTest {
    override val commandName: String = "pull"

    @Test
    override fun checkExists() {
        val result = runCommand(commandName)
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("SMP installation identifier is not configured"))
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *directConnectorArgs()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *connectorArgsByConfigFileInPath()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkEmptyExecution() {
        val result = runCommand(commandName, *connectorArgsByConfigFile())
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains("Sources must be specified"))
    }

    @Test
    override fun checkScriptsExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        assertPulledScriptExists("testScript2")
    }

    @Test
    override fun checkModulesExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledModuleExists("testModule1")
        assertPulledModuleExists("testModule2")
    }

    @Test
    override fun checkFullExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        assertPulledScriptExists("testScript2")
        assertPulledModuleExists("testModule1")
        assertPulledModuleExists("testModule2")
    }
}
