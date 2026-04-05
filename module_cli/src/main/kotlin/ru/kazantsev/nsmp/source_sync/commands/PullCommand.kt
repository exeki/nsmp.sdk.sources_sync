package ru.kazantsev.nsmp.source_sync.commands

import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
class PullCommand : AbstractCommand("pull", "Download source files from installation") {
    override fun execute() {
        println("Executing pull command...")
        val fetched = getService().pull(scripts, modules)
        println("Fetched scripts=${fetched.scripts.size}, modules=${fetched.modules.size}")
    }
}
