package ru.kazantsev.nsmp.source_sync.cli

import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
class PullCommand : AbstractCommand("pull", "Download source files from installation") {

    override fun execute() {
        println("Executing pull command...")
        val pull = getService().pull(createRequest())
        println("Fetched scripts=${pull.scripts.size}, modules=${pull.modules.size}, advImports=${pull.advImports.size}")
    }
}
