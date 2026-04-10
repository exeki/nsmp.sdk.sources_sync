package ru.kazantsev.nsmp.source_sync.cli

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcConnector
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.nio.file.Paths

@OptIn(ExperimentalCli::class)
abstract class AbstractCommand(
    name: String,
    description: String
) : Subcommand(name, description) {

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

    protected val ignoreSsl: Boolean
        get() = parseBooleanOption("ignoreSsl", ignoreSslRaw)

    protected fun createConnectorParams(): ConnectorParams {
        return if (installationId.isNotEmpty() && configPath.isNotEmpty()) {
            ConnectorParams.byConfigFileInPath(installationId, configPath)
        } else if (installationId.isNotEmpty() && scheme.isNotEmpty() && host.isNotEmpty() && accessKey.isNotEmpty()) {
            ConnectorParams(installationId, scheme, host, accessKey, ignoreSsl)
        } else if (installationId.isNotEmpty()) ConnectorParams.byConfigFile(installationId)
        else throw IllegalStateException("SMP installation identifier is not configured")
    }

    protected fun getService(): SrcService {
        val connector = SrcConnector(createConnectorParams())
        val projectPath = Paths.get(projectPath)
        return SrcService(connector, ObjectMapper(), projectPath)
    }

    protected fun createRequest(): SrcRequest {
        return SrcRequest(
            modules = parseCsv(modulesCsv),
            allModules = parseBooleanOption("allModules", allModulesRaw),
            scripts = parseCsv(scriptsCsv),
            allScripts = parseBooleanOption("allScripts", allScriptsRaw),
            advImports = parseCsv(advImportsCsv),
            allAdvImports = parseBooleanOption("allAdvImports", allAdvImportsRaw)
        )
    }

    private fun parseCsv(value: String): List<String> {
        return value.split(',')
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
}
