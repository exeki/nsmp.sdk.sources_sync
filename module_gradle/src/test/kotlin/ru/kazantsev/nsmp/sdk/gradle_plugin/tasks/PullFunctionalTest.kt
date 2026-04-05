package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.gradle_plugin.PluginFunctionalTestBase
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

        assertTrue(result.output.contains("Sources must be specified"))
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

    private fun assertPulledScriptExists(code: String) {
        val scriptsRoot = testProjectDir.resolve("src/main/scripts")
        val found = Files.walk(scriptsRoot).use { pathStream ->
            pathStream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy" }
        }
        assertTrue(found, "Expected script file for code=$code to be created in $scriptsRoot")
    }

    private fun assertPulledModuleExists(code: String) {
        val modulesRoot = testProjectDir.resolve("src/main/modules")
        val found = Files.walk(modulesRoot).use { pathStream ->
            pathStream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy" }
        }
        assertTrue(
            found,
            "Expected module file for code=$code to be created in $modulesRoot"
        )
    }
}
