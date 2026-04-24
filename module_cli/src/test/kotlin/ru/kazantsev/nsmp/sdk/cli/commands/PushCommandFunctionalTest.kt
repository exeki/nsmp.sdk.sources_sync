package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import ru.kazantsev.nsmp.source_sync.cli.AbstractCommand
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptySrcRequestException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PushCommandFunctionalTest : CommandFunctionalTestBase(), ICommandTest {
    override val commandName: String = "push"

    @Test
    override fun checkExists() {
        val result = runCommand(commandName)
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains(AbstractCommand.INSTALLATION_ID_NOT_CONFIGURED_MSG))
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        createLocalScript("testScript1")
        createLocalModule("testModule1")
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            CommandArgs.FORCE.withValue("true"),
            *directConnectorArgs()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        createLocalScript("testScript1")
        createLocalModule("testModule1")
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFileInPath()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        createLocalScript("testScript1")
        createLocalModule("testModule1")
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1"),
            CommandArgs.MODULES.withValue("testModule1"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkEmptyExecution() {
        val result = runCommand(commandName, *connectorArgsByConfigFile())
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains(EmptySrcRequestException.MSG))
    }

    @Test
    override fun checkScriptsExecution() {
        createLocalScript("testScript1")
        createLocalScript("testScript2")
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkModulesExecution() {
        createLocalModule("testModule1")
        createLocalModule("testModule2")
        val result = runCommand(
            commandName,
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkFullExecution() {
        createLocalScript("testScript1")
        createLocalScript("testScript2")
        createLocalModule("testModule1")
        createLocalModule("testModule2")
        val result = runCommand(
            commandName,
            CommandArgs.SCRIPTS.withValue("testScript1,testScript2"),
            CommandArgs.MODULES.withValue("testModule1,testModule2"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkAllModulesExecution() {
        createLocalModule("testModule1")
        createLocalModule("testModule2")
        val result = runCommand(
            commandName,
            CommandArgs.ALL_MODULES.withValue("true"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkAllScriptsExecution() {
        createLocalScript("testScript1")
        createLocalScript("testScript2")
        val result = runCommand(
            commandName,
            CommandArgs.ALL_SCRIPTS.withValue("true"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        createLocalAdvImport("testImport1")
        val result = runCommand(
            commandName,
            CommandArgs.ALL_ADV_IMPORTS.withValue("true"),
            CommandArgs.FORCE.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
    }
}
