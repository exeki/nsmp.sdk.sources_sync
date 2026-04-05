package ru.kazantsev.nsmp.source_sync.commands

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcConnector
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService
import java.nio.file.Paths

@OptIn(ExperimentalCli::class)
abstract class AbstractCommand(
    name: String,
    description: String
) : Subcommand(name, description) {
    companion object {
        private val ALLOWED_LOG_LEVELS = setOf("trace", "debug", "info", "warn", "error")
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

    protected val ignoreSsl by option(
        ArgType.Boolean,
        fullName = "ignoreSsl",
        description = "Ignore SSL"
    ).default(false)

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

    protected val scripts: List<String>
        get() = parseCsv(scriptsCsv)

    protected val modules: List<String>
        get() = parseCsv(modulesCsv)

    protected fun createConnectorParams(): ConnectorParams {
        return if (installationId.isNotEmpty() && configPath.isNotEmpty()) {
            ConnectorParams.byConfigFileInPath(installationId, configPath)
        } else if (installationId.isNotEmpty() && scheme.isNotEmpty() && host.isNotEmpty() && accessKey.isNotEmpty()) {
            ConnectorParams(installationId, scheme, host, accessKey, ignoreSsl)
        } else if (installationId.isNotEmpty()) ConnectorParams.byConfigFile(installationId)
        else throw IllegalStateException("SMP installation identifier is not configured")
    }

    protected fun getService(): SrcService {
        val normalizedLogLevel = logLevel.lowercase()
        require(normalizedLogLevel in ALLOWED_LOG_LEVELS) {
            "Unsupported --log-level: $logLevel. Allowed values: ${ALLOWED_LOG_LEVELS.joinToString(", ")}"
        }
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", normalizedLogLevel)
        val connector = SrcConnector(createConnectorParams())
        val projectPath = Paths.get(projectPath)
        return SrcService(connector, ObjectMapper(), projectPath)
    }

    private fun parseCsv(value: String): List<String> {
        return value.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
