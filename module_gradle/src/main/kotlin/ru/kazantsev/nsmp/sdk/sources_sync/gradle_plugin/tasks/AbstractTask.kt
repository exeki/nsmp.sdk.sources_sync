package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

abstract class AbstractTask : DefaultTask() {
    companion object {
        const val INSTALLATION_ID_REQUIRED_MSG =
            "Option --installationId is required when connector options are provided explicitly"
        const val CONNECTOR_MODES_VALIDATION_MSG =
            "Connector options must use either config file mode (--installationId, --configPath) or direct mode (--installationId, --scheme, --host, --accessKey, optional --ignoreSsl)"
        const val DIRECT_CONNECTOR_MODE_REQUIRED_MSG = "Direct connector mode requires"
    }

    @get:Internal
    var connectorParamsProvider: Provider<ConnectorParams>? = null

    @get:Internal
    var srcFoldersParamsProvider: Provider<SrcFoldersParams>? = null

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

    @get:Input
    @get:Optional
    @get:Option(option = "advImports", description = "Advanced import codes separated by comma")
    abstract val advImports: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "allModules", description = "Use all local modules")
    abstract val allModules: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "allScripts", description = "Use all local scripts")
    abstract val allScripts: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "allAdvImports", description = "Use all local advanced imports")
    abstract val allAdvImports: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "modulesExcluded", description = "Module codes to exclude, separated by comma")
    abstract val modulesExcluded: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "scriptsExcluded", description = "Script codes to exclude, separated by comma")
    abstract val scriptsExcluded: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "advImportsExcluded", description = "Advanced import codes to exclude, separated by comma")
    abstract val advImportsExcluded: Property<String>

    protected fun createConnectorParams(): ConnectorParams {
        validateConnectorOptions()
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

    protected fun createService(): SrcSyncService {
        return SrcSyncService(
            resolveConnectorParams(),
            resolveSrcFoldersParams()
        )
    }

    protected fun resolveSrcFoldersParams(): SrcFoldersParams {
        return srcFoldersParamsProvider?.orNull ?: SrcFoldersParams(project.projectDir.path)
    }

    protected fun resolveConnectorParams(): ConnectorParams {
        validateConnectorOptions()
        return if (installationId.isPresent) createConnectorParams()
        else connectorParamsProvider?.orNull
            ?: throw IllegalStateException("SMP connection parameters are not configured")
    }


    protected fun parseCsvOption(value: String?): Set<String> {
        return value
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    protected fun parseBooleanOption(name: String, value: String): Boolean {
        return when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Option --$name must be 'true' or 'false', but was '$value'")
        }
    }

    protected fun createRequest(): SrcRequest {
        return SrcRequest(
            modules = parseCsvOption(modules.orNull),
            allModules = parseBooleanOption("allModules", allModules.orNull ?: "false"),
            modulesExcluded = parseCsvOption(modulesExcluded.orNull),
            scripts = parseCsvOption(scripts.orNull),
            allScripts = parseBooleanOption("allScripts", allScripts.orNull ?: "false"),
            scriptsExcluded = parseCsvOption(scriptsExcluded.orNull),
            advImports = parseCsvOption(advImports.orNull),
            allAdvImports = parseBooleanOption("allAdvImports", allAdvImports.orNull ?: "false"),
            advImportsExcluded = parseCsvOption(advImportsExcluded.orNull),
        )
    }

    init {
        group = "nsmp_sdk_sources_sync"
    }

    private fun validateConnectorOptions() {
        val hasInstallationId = installationId.isPresent
        val hasConfigPath = configurationPath.isPresent
        val hasDirectOptions = scheme.isPresent || host.isPresent || accessKey.isPresent || ignoreSsl.isPresent
        val hasAnyConnectorOverride = hasConfigPath || hasDirectOptions

        if (!hasInstallationId && hasAnyConnectorOverride) {
            throw IllegalArgumentException(INSTALLATION_ID_REQUIRED_MSG)
        }

        if (hasConfigPath && hasDirectOptions) {
            throw IllegalArgumentException(CONNECTOR_MODES_VALIDATION_MSG)
        }

        if (hasDirectOptions) {
            val missingOptions = buildList {
                if (!scheme.isPresent) add("--scheme")
                if (!host.isPresent) add("--host")
                if (!accessKey.isPresent) add("--accessKey")
            }
            if (missingOptions.isNotEmpty()) {
                throw IllegalArgumentException(
                    "$DIRECT_CONNECTOR_MODE_REQUIRED_MSG ${missingOptions.joinToString()}"
                )
            }
        }
    }
}
