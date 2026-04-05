package ru.kazantsev.nsmp.source_sync.commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default

@OptIn(ExperimentalCli::class)
class PushCommand : AbstractCommand("push", "Upload local source files to installation") {
    private val force by option(
        ArgType.Boolean,
        fullName = "force",
        description = "Push without sync check"
    ).default(false)

    override fun execute() {
        val push = getService().push(scripts, modules, force)
        println("Push scripts=${push.scripts.size}, modules=${push.modules.size}")
    }
}
