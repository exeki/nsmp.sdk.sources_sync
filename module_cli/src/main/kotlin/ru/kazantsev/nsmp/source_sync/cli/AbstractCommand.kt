package ru.kazantsev.nsmp.source_sync.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

@OptIn(ExperimentalCli::class)
abstract class AbstractCommand(
    name: String,
    description: String
) : Subcommand(name, description) {

    companion object {
        private const val SIMPLE_LOGGER_DEFAULT_LEVEL_PROPERTY = "org.slf4j.simpleLogger.defaultLogLevel"
        private val ALLOWED_LOG_LEVELS = setOf("trace", "debug", "info", "warn", "error")
        const val INSTALLATION_ID_NOT_CONFIGURED_MSG = "SMP installation identifier is not configured"
        const val INSTALLATION_ID_REQUIRED_MSG = "Option --installationId is required"
        const val CONNECTOR_MODES_VALIDATION_MSG = "Connector options must use either config file mode (--installationId, --configPath) or direct mode (--installationId, --scheme, --host, --accessKey, optional --ignoreSsl)"
        const val DIRECT_CONNECTOR_MODE_REQUIRED_MSG = "Direct connector mode requires"

        fun configureSimpleLoggerLogLevel(args: Array<String>) {
            val requestedLogLevel = resolveLogLevelArg(args) ?: return
            require(requestedLogLevel in ALLOWED_LOG_LEVELS) {
                "Option --log-level must be one of: ${ALLOWED_LOG_LEVELS.joinToString()}"
            }
            System.setProperty(SIMPLE_LOGGER_DEFAULT_LEVEL_PROPERTY, requestedLogLevel)
        }

        private fun resolveLogLevelArg(args: Array<String>): String? {
            for (i in args.indices) {
                val arg = args[i]
                if (arg.startsWith("--log-level=")) {
                    return arg.substringAfter('=').lowercase()
                }
                if (arg == "--log-level" && i + 1 < args.size) {
                    return args[i + 1].lowercase()
                }
            }
            return null
        }
    }

    protected val installationId by option(
        ArgType.String,
        fullName = "installationId",
        description = "installation Id"
    ).default("")

    protected val configPath by option(
        ArgType.String,
        fullName = "configPath",
        description = "Path to connector config file"
    ).default("")

    protected val scheme by option(
        ArgType.String,
        fullName = "scheme",
        description = "Connection scheme (http or https)"
    ).default("")

    protected val host by option(
        ArgType.String,
        fullName = "host",
        description = "Installation host"
    ).default("")

    protected val accessKey by option(
        ArgType.String,
        fullName = "accessKey",
        description = "Access key for installation"
    ).default("")

    private val ignoreSslRaw by option(
        ArgType.String,
        fullName = "ignoreSsl",
        description = "Ignore SSL (true|false)"
    ).default("false")

    protected val projectPath by option(
        ArgType.String,
        fullName = "projectPath",
        description = "Path to project"
    ).default("")

    @Suppress("unused")
    protected val logLevel by option(
        ArgType.String,
        fullName = "log-level",
        description = "Log level: trace, debug, info, warn, error"
    ).default("info")

    private val scriptsCsv by option(
        ArgType.String,
        fullName = "scripts",
        description = "Script codes separated by comma"
    ).default("")

    private val modulesCsv by option(
        ArgType.String,
        fullName = "modules",
        description = "Module codes separated by comma"
    ).default("")

    private val advImportsCsv by option(
        ArgType.String,
        fullName = "advImports",
        description = "Advanced import codes separated by comma"
    ).default("")

    private val allModulesRaw by option(
        ArgType.String,
        fullName = "allModules",
        description = "Use all local modules (true|false)"
    ).default("false")

    private val allScriptsRaw by option(
        ArgType.String,
        fullName = "allScripts",
        description = "Use all local scripts (true|false)"
    ).default("false")

    private val allAdvImportsRaw by option(
        ArgType.String,
        fullName = "allAdvImports",
        description = "Use all local adv imports (true|false)"
    ).default("false")

    private val modulesExcludedCsv by option(
        ArgType.String,
        fullName = "modulesExcluded",
        description = "Module codes to exclude, separated by comma"
    ).default("")

    private val scriptsExcludedCsv by option(
        ArgType.String,
        fullName = "scriptsExcluded",
        description = "Script codes to exclude, separated by comma"
    ).default("")

    private val advImportsExcludedCsv by option(
        ArgType.String,
        fullName = "advImportsExcluded",
        description = "Advanced import codes to exclude, separated by comma"
    ).default("")

    protected val ignoreSsl: Boolean
        get() = parseBooleanOption("ignoreSsl", ignoreSslRaw)

    protected fun createConnectorParams(): ConnectorParams {
        validateConnectorOptions()
        return if (installationId.isNotEmpty() && configPath.isNotEmpty()) {
            ConnectorParams.byConfigFileInPath(installationId, configPath)
        } else if (installationId.isNotEmpty() && scheme.isNotEmpty() && host.isNotEmpty() && accessKey.isNotEmpty()) {
            ConnectorParams(installationId, scheme, host, accessKey, ignoreSsl)
        } else if (installationId.isNotEmpty()) ConnectorParams.byConfigFile(installationId)
        else throw IllegalStateException(INSTALLATION_ID_NOT_CONFIGURED_MSG)
    }

    protected fun getService(): SrcSyncService {
        return SrcSyncService(createConnectorParams(),  SrcFoldersParams(projectPath))
    }

    protected fun createRequest(): SrcRequest {
        return SrcRequest(
            modules = parseCsv(modulesCsv),
            allModules = parseBooleanOption("allModules", allModulesRaw),
            modulesExcluded = parseCsv(modulesExcludedCsv),
            scripts = parseCsv(scriptsCsv),
            allScripts = parseBooleanOption("allScripts", allScriptsRaw),
            scriptsExcluded = parseCsv(scriptsExcludedCsv),
            advImports = parseCsv(advImportsCsv),
            allAdvImports = parseBooleanOption("allAdvImports", allAdvImportsRaw),
            advImportsExcluded = parseCsv(advImportsExcludedCsv),
        )
    }

    private fun parseCsv(value: String): Set<String> {
        return value.split(',')
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

    private fun validateConnectorOptions() {
        val hasInstallationId = installationId.isNotEmpty()
        val hasConfigPath = configPath.isNotEmpty()
        val hasDirectOptions = scheme.isNotEmpty() || host.isNotEmpty() || accessKey.isNotEmpty() || ignoreSslRaw != "false"

        if (!hasInstallationId) {
            throw IllegalArgumentException(INSTALLATION_ID_REQUIRED_MSG)
        }

        if (hasConfigPath && hasDirectOptions) {
            throw IllegalArgumentException(CONNECTOR_MODES_VALIDATION_MSG)
        }

        if (hasDirectOptions) {
            val missingOptions = buildList {
                if (scheme.isEmpty()) add("--scheme")
                if (host.isEmpty()) add("--host")
                if (accessKey.isEmpty()) add("--accessKey")
            }
            if (missingOptions.isNotEmpty()) {
                throw IllegalArgumentException(
                    "$DIRECT_CONNECTOR_MODE_REQUIRED_MSG ${missingOptions.joinToString()}"
                )
            }
        }
    }
}
