package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.PluginFunctionalTestBase

class PushTaskFunctionalTest : PluginFunctionalTestBase(), ITaskTest {

    override val taskName: String = PushTask.NAME

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
        assertTrue(result.output.contains(TaskArgs.FORCE.flag))
    }

    @Test
    override fun checkCliConnectorParamsDirect() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true"),
            *connectorParamsDirectArgs()
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkCliConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true"),
            *connectorParamsByConfigFileInPathArgs()
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkCliConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true"),
            *connectorParamsByConfigFileArgs()
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkExtensionConnectorParamsDirect() {
        writeConsumerProject()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFileInPath() {
        writeConsumerProjectWithConfigInPath()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkExtensionConnectorParamsByConfigFile() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalModule("testModule1")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.MODULES.withValue("testModule1"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkEmptyExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(taskName).buildAndFail()

        assertTrue(result.output.contains("No local sources found to push"))
    }

    @Test
    override fun checkScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalScript("testScript2")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalModule("testModule1")
        createLocalModule("testModule2")

        val result = runner(
            taskName,
            TaskArgs.MODULES.withValue("testModule1,testModule2"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkFullExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalScript("testScript2")
        createLocalModule("testModule1")
        createLocalModule("testModule2")

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2"),
            TaskArgs.MODULES.withValue("testModule1,testModule2"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkAllModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalModule("testModule1")
        createLocalModule("testModule2")

        val result = runner(
            taskName,
            TaskArgs.ALL_MODULES.withValue("true"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkAllScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalScript("testScript1")
        createLocalScript("testScript2")

        val result = runner(
            taskName,
            TaskArgs.ALL_SCRIPTS.withValue("true"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createLocalAdvImport("testImport1")

        val result = runner(
            taskName,
            TaskArgs.ALL_ADV_IMPORTS.withValue("true"),
            TaskArgs.FORCE.withValue("true")
        ).build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
