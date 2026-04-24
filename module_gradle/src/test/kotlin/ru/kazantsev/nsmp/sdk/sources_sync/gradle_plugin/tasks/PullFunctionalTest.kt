package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptySrcRequestException
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

class PullFunctionalTest : PluginFunctionalTestBase(), ITaskTest {

    override val taskName: String = PullTask.NAME

    @Test
    override fun checkExists() {
        writeConsumerProject()

        val result = runner("help", "--task", taskName).build()

        assertTrue(result.output.contains(taskName))
        assertTrue(result.output.contains(TaskArgs.INSTALLATION_ID.flag))
        assertTrue(result.output.contains(TaskArgs.CONFIG_PATH.flag))
        assertTrue(result.output.contains(TaskArgs.SCHEME.flag))
        assertTrue(result.output.contains(TaskArgs.HOST.flag))
        assertTrue(result.output.contains(TaskArgs.ACCESS_KEY.flag))
        assertTrue(result.output.contains(TaskArgs.IGNORE_SSL.flag))
        assertTrue(result.output.contains(TaskArgs.SCRIPTS.flag))
        assertTrue(result.output.contains(TaskArgs.MODULES.flag))
        assertTrue(result.output.contains(TaskArgs.ADV_IMPORTS.flag))
        assertTrue(result.output.contains(TaskArgs.ALL_SCRIPTS.flag))
        assertTrue(result.output.contains(TaskArgs.ALL_MODULES.flag))
        assertTrue(result.output.contains(TaskArgs.ALL_ADV_IMPORTS.flag))
        assertTrue(result.output.contains(TaskArgs.SCRIPTS_EXCLUDED.flag))
        assertTrue(result.output.contains(TaskArgs.MODULES_EXCLUDED.flag))
        assertTrue(result.output.contains(TaskArgs.ADV_IMPORTS_EXCLUDED.flag))
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsDirectArgs()
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsByConfigFileInPathArgs()
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsByConfigFileArgs()
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkExtensionConnectorParamsDirect() {
        writeConsumerProject()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithConfigInPath()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkEmptyExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(taskName).buildAndFail()

        assertTrue(result.output.contains(EmptySrcRequestException.MSG))
    }

    @Test
    override fun checkScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2")
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledScriptExists("testScript2")
    }

    @Test
    override fun checkModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(
            taskName,
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        assertPulledModuleExists("testModule1")
        assertPulledModuleExists("testModule2")
    }

    @Test
    override fun checkAllModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.ALL_MODULES.withValue("true")
        ).build()

        assertPulledModuleExists("testModule1")
    }

    @Test
    override fun checkAllScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.ALL_SCRIPTS.withValue("true")
        ).build()

        assertPulledScriptExists("testScript1")
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.ALL_ADV_IMPORTS.withValue("true")
        ).build()

        assertPulledAdvImportExists("testImport1")
    }

    @Test
    override fun checkFullExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2"),
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertPulledScriptExists("testScript1")
        assertPulledScriptExists("testScript2")
        assertPulledModuleExists("testModule1")
        assertPulledModuleExists("testModule2")
    }

    @Test
    fun failsWhenModuleDoesNotExist() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(
            taskName,
            TaskArgs.MODULES.withValue("module_does_not_exist_7f3b9b16")
        ).buildAndFail()

        assertTrue(result.output.contains("BUILD FAILED"))
    }

    @Test
    fun checkAllScriptsWithScriptsExcludedExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        runner(
            taskName,
            TaskArgs.ALL_SCRIPTS.withValue("true"),
            TaskArgs.SCRIPTS_EXCLUDED.withValue("testScript2")
        ).build()

        assertPulledScriptExists("testScript1")
        val scriptsRoot = testProjectDir.resolve(SrcFoldersParams.getDefaultRelativeScriptsPathString())
        val foundExcluded = Files.walk(scriptsRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "testScript2.groovy"
            }
        }
        assertTrue(!foundExcluded, "Expected script testScript2 to be excluded from pull")
    }
}
