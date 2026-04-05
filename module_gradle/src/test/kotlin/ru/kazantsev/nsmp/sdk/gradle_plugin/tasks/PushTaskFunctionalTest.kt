package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

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

        assertTrue(result.output.contains("No sources found to upload"))
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

    private fun createLocalScript(code: String) {
        val scriptPath = testProjectDir.resolve("src/main/scripts/ru/kazantsev/demo/$code.groovy")
        Files.createDirectories(scriptPath.parent)
        Files.writeString(
            scriptPath,
            """
            package ru.kazantsev.demo
            
            class $code {}
            """.trimIndent()
        )
    }

    private fun createLocalModule(code: String) {
        val modulePath = testProjectDir.resolve("src/main/modules/ru/kazantsev/demo/$code.groovy")
        Files.createDirectories(modulePath.parent)
        Files.writeString(
            modulePath,
            """
            package ru.kazantsev.demo
            
            class $code {}
            """.trimIndent()
        )
    }
}
