package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcConnector
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService
import java.nio.file.Paths

abstract class AbstractTask : DefaultTask() {

    @get:Internal
    var connectorParamsProvider: Provider<ConnectorParams>? = null

    @get:Input
    @get:Optional
    @get:Option(option = "installationId", description = "NSD installation identifier")
    abstract val installationId: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "configPath", description = "Path to config file")
    abstract val configurationPath: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "scheme", description = "Connection scheme, for example http or https")
    abstract val scheme: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "host", description = "NSD host")
    abstract val host: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "accessKey", description = "NSD access key")
    abstract val accessKey: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "ignoreSsl", description = "Ignore SSL validation")
    abstract val ignoreSsl: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "scripts", description = "Script codes separated by comma")
    abstract val scripts: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "modules", description = "Module codes separated by comma")
    abstract val modules: Property<String>

    protected fun createConnectorParams(): ConnectorParams {
        return if (installationId.isPresent && configurationPath.isPresent) {
            ConnectorParams.byConfigFileInPath(installationId.get(), configurationPath.get())
        } else if (installationId.isPresent && scheme.isPresent && host.isPresent && accessKey.isPresent) {
            ConnectorParams(
                installationId.get(),
                scheme.get(),
                host.get(),
                accessKey.get(),
                parseBooleanOption("ignoreSsl", ignoreSsl.orNull ?: "false")
            )
        } else if (installationId.isPresent) {
            ConnectorParams.byConfigFile(installationId.get())
        } else {
            throw IllegalStateException("NSMP installation identifier is not configured")
        }
    }

    protected fun createService(): SrcService {
        val connector = SrcConnector(resolveConnectorParams())
        val projectPath = Paths.get(project.projectDir.path)
        return SrcService(connector, ObjectMapper(), projectPath)
    }

    protected fun resolveConnectorParams(): ConnectorParams {
        return if (installationId.isPresent) createConnectorParams()
        else connectorParamsProvider?.orNull
            ?: throw IllegalStateException("SMP connection parameters are not configured")
    }

    protected fun parseCsvOption(value: String?): List<String> {
        return value
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    protected fun parseBooleanOption(name: String, value: String): Boolean {
        return when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Option --$name must be 'true' or 'false', but was '$value'")
        }
    }

    init {
        group = "nsmp_sdk_sources_sync"
    }
}
