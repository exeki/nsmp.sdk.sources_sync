package ru.kazantsev.nsmp.source_sync.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default

@OptIn(ExperimentalCli::class)
class PushCommand : AbstractCommand("push", "Upload local source files to installation") {
    private val forceRaw by option(
        ArgType.String,
        fullName = "force",
        description = "Push without sync check (true|false)"
    ).default("false")

    private val force: Boolean
        get() = parseBooleanOption("force", forceRaw)

    override fun execute() {
        println("Executing push command...")
        val push = getService().push(createRequest(), force)
        println("Push scripts=${push.scripts.size}, modules=${push.modules.size}, advImports=${push.advImports.size}")
    }
}
