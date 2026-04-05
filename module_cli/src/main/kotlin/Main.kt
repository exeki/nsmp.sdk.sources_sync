package ru.kazantsev.nsmp.sdk

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import ru.kazantsev.nsmp.source_sync.commands.PullCommand
import ru.kazantsev.nsmp.source_sync.commands.PushCommand
import ru.kazantsev.nsmp.source_sync.commands.SyncCheckCommand
import java.io.PrintStream
import kotlin.system.exitProcess


@OptIn(ExperimentalCli::class)
fun runCli(
    args: Array<String>,
    err: PrintStream = System.err
): Int {
    try {
        val parser = ArgParser("sources-sync")
        parser.subcommands(
            PullCommand(),
            PushCommand(),
            SyncCheckCommand()
        )
        parser.parse(args)
        return 0
    } catch (ex: Exception) {
        err.println(ex.message ?: "Unexpected error")
        ex.printStackTrace(err)
        return 1
    }
}

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    exitProcess(runCli(args))
}
