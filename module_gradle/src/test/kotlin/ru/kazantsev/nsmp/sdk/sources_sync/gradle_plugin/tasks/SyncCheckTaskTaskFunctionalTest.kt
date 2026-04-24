package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptySrcRequestException
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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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
        createSyncCheckFixture(
            scripts = listOf("testScript1"),
            modules = listOf("testModule1")
        )

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

        assertTrue(result.output.contains(EmptySrcRequestException.MSG))
    }

    @Test
    override fun checkScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(scripts = listOf("testScript1", "testScript2"))

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
    }

    @Test
    override fun checkModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(modules = listOf("testModule1", "testModule2"))

        val result = runner(
            taskName,
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkAllModulesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(modules = listOf("testModule1"))

        val result = runner(
            taskName,
            TaskArgs.ALL_MODULES.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed module: testModule1"))
    }

    @Test
    override fun checkAllScriptsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(scripts = listOf("testScript1"))

        val result = runner(
            taskName,
            TaskArgs.ALL_SCRIPTS.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
    }

    @Test
    override fun checkAllAdvImportsExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(advImports = listOf("testImport1"))

        val result = runner(
            taskName,
            TaskArgs.ALL_ADV_IMPORTS.withValue("true")
        ).build()

        assertTrue(result.output.contains("Changed adv import: testImport1"))
    }

    @Test
    override fun checkFullExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()
        createSyncCheckFixture(
            scripts = listOf("testScript1", "testScript2"),
            modules = listOf("testModule1", "testModule2")
        )

        val result = runner(
            taskName,
            TaskArgs.SCRIPTS.withValue("testScript1,testScript2"),
            TaskArgs.MODULES.withValue("testModule1,testModule2")
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }
}
