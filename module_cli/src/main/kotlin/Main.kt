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

@OptIn(ExperimentalCli::class)
fun runCli(
    args: Array<String>,
    err: PrintStream = System.err
): Int {
    AbstractCommand.configureSimpleLoggerLogLevel(args)
    val logger = LoggerFactory.getLogger("CLI")
    try {
        logger.debug("args: ${args.joinToString(" ")}")
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

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    exitProcess(runCli(args))
}
