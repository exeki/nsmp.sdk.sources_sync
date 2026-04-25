package ru.kazantsev.nsmp.sdk

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.source_sync.cli.AbstractCommand
import ru.kazantsev.nsmp.source_sync.cli.PullCommand
import ru.kazantsev.nsmp.source_sync.cli.PushCommand
import ru.kazantsev.nsmp.source_sync.cli.SyncCheckCommand
import java.io.PrintStream
import kotlin.system.exitProcess

private val SENSITIVE_CLI_OPTIONS = setOf("--accesskey")

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    exitProcess(runCli(args))
}

@OptIn(ExperimentalCli::class)
fun runCli(
    args: Array<String>,
    err: PrintStream = System.err
): Int {
    AbstractCommand.configureSimpleLoggerLogLevel(args)
    val logger = LoggerFactory.getLogger("CLI")
    try {
        logger.debug("args: ${sanitizeCliArgs(args).joinToString(" ")}")
        val parser = ArgParser("sources-sync")
        parser.subcommands(
            PullCommand(),
            PushCommand(),
            SyncCheckCommand()
        )
        logger.info("CLI command started")
        logger.debug("start parse")
        parser.parse(args)
        return 0
    } catch (ex: Exception) {
        logger.error(ex.message, ex)
        err.println(ex.message ?: "Unexpected error")
        return 1
    }
}

private fun sanitizeCliArgs(args: Array<String>): Array<String> {
    val sanitized = args.copyOf()
    var i = 0
    while (i < sanitized.size) {
        val arg = sanitized[i]
        val optionName = arg.substringBefore('=').lowercase()

        if (optionName in SENSITIVE_CLI_OPTIONS) {
            if (arg.contains('=')) {
                sanitized[i] = "${arg.substringBefore('=')}=***"
            } else if (i + 1 < sanitized.size) {
                sanitized[i + 1] = "***"
                i++
            }
        }
        i++
    }
    return sanitized
}
