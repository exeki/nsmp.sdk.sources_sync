package ru.kazantsev.nsmp.sdk

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.source_sync.cli.PullCommand
import ru.kazantsev.nsmp.source_sync.cli.PushCommand
import ru.kazantsev.nsmp.source_sync.cli.SyncCheckCommand
import java.io.PrintStream
import kotlin.system.exitProcess

private val ALLOWED_LOG_LEVELS = setOf("trace", "debug", "info", "warn", "error")

@OptIn(ExperimentalCli::class)
fun runCli(
    args: Array<String>,
    err: PrintStream = System.err
): Int {
    val requestedLogLevel = resolveLogLevelArg(args)
    if (requestedLogLevel != null && requestedLogLevel in ALLOWED_LOG_LEVELS) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", requestedLogLevel)
    }
    val logger = LoggerFactory.getLogger("CLI")
    try {
        logger.debug("args: ${args.joinToString(" ")}")
        val parser = ArgParser("sources-sync")
        parser.subcommands(
            PullCommand(),
            PushCommand(),
            SyncCheckCommand()
        )
        logger.debug("start parse")
        parser.parse(args)
        return 0
    } catch (ex: Exception) {
        logger.error(ex.message, ex)
        err.println(ex.message ?: "Unexpected error")
        return 1
    }
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

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    exitProcess(runCli(args))
}
