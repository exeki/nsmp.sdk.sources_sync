package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.PluginFunctionalTestBase

class ConnectorParamsValidationFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun checkMixedConnectorModesExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(
            PullTask.NAME,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.INSTALLATION_ID.withDefaultValue(),
            TaskArgs.CONFIG_PATH.withDefaultValue(),
            TaskArgs.SCHEME.withDefaultValue(),
            TaskArgs.HOST.withDefaultValue(),
            TaskArgs.ACCESS_KEY.withDefaultValue()
        ).buildAndFail()

        assertTrue(result.output.contains("Connector options must use either config file mode"))
    }

    @Test
    fun checkIncompleteDirectConnectorModeExecution() {
        writeConsumerProjectWithInstallationOnlyConfig()

        val result = runner(
            PullTask.NAME,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.INSTALLATION_ID.withDefaultValue(),
            TaskArgs.SCHEME.withDefaultValue(),
            TaskArgs.HOST.withDefaultValue()
        ).buildAndFail()

        assertTrue(result.output.contains("Direct connector mode requires --accessKey"))
    }

    @Test
    fun checkConnectorOverridesWithoutInstallationIdExecution() {
        writeConsumerProject()

        val result = runner(
            PullTask.NAME,
            TaskArgs.SCRIPTS.withValue("testScript1"),
            TaskArgs.CONFIG_PATH.withDefaultValue()
        ).buildAndFail()

        assertTrue(result.output.contains("Option --installationId is required"))
    }
}
