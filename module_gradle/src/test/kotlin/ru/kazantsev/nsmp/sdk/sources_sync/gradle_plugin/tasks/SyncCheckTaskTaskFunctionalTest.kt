package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.PluginFunctionalTestBase

class SyncCheckTaskTaskFunctionalTest : PluginFunctionalTestBase(), ITaskTest {

    override val taskName: String = SyncCheckTask.NAME

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
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsDirectArgs()
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsByConfigFileInPathArgs()
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            *connectorParamsByConfigFileArgs()
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkExtensionConnectorParamsDirect() {
        writeConsumerProject()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithConfigInPath()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
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
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
    }

    @Test
    override fun checkModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkAllModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.ALL_MODULES.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkAllScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.ALL_SCRIPTS.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.ALL_ADV_IMPORTS.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed adv import: testImport1"))
    }

    @Test
    override fun checkFullExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        writeLocalInfoFile()

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2"),
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }
}
