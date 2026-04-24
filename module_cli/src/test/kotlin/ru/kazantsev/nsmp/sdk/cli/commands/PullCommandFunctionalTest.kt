package ru.kazantsev.nsmp.sdk.cli.commands

import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.cli.CommandFunctionalTestBase
import ru.kazantsev.nsmp.source_sync.cli.AbstractCommand
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptySrcRequestException
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PullCommandFunctionalTest : CommandFunctionalTestBase(), ICommandTest {
    override val commandName: String = "pull"

    @Test
    override fun checkExists() {
        val result = runCommand(commandName)
        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.contains(AbstractCommand.INSTALLATION_ID_NOT_CONFIGURED_MSG))
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
        assertTrue(result.stderr.contains(EmptySrcRequestException.MSG))
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

    @Test
    override fun checkAllModulesExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.ALL_MODULES.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkAllScriptsExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.ALL_SCRIPTS.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.ALL_ADV_IMPORTS.withValue("true"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledAdvImportExists("testImport1")
    }

    @Test
    fun checkAllScriptsWithScriptsExcludedExecution() {
        val result = runCommand(
            commandName,
            CommandArgs.ALL_SCRIPTS.withValue("true"),
            CommandArgs.SCRIPTS_EXCLUDED.withValue("testScript2"),
            *connectorArgsByConfigFile()
        )
        assertEquals(0, result.exitCode)
        assertPulledScriptExists("testScript1")
        val excludedScript = testProjectDir.resolve(SrcFoldersParams.getDefaultRelativeScriptsPathString())
        val foundExcluded = Files.walk(excludedScript).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "testScript2.groovy"
            }
        }
        assertTrue(!foundExcluded, "Expected script testScript2 to be excluded from pull")
    }
}
