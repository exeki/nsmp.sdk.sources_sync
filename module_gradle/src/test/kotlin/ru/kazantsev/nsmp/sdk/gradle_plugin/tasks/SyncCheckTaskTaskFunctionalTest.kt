package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

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
        Files.createDirectories(testProjectDir.resolve("src/main/scripts"))
        Files.createDirectories(testProjectDir.resolve("src/main/modules"))

        val result = runner(taskName).buildAndFail()

        assertTrue(result.output.contains("No sources found to sync check"))
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
