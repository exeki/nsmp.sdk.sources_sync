package ru.kazantsev.nsmp.source_sync.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequestWithExclusion

@OptIn(ExperimentalCli::class)
class PullCommand : AbstractCommand("pull", "Download source files from installation") {

    override fun execute() {
        println("Executing pull command...")
        val pull = getService().pull(createRequest())
        println("Fetched scripts=${pull.scripts.size}, modules=${pull.modules.size}, advImports=${pull.advImports.size}")
    }
}
