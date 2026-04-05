package ru.kazantsev.nsmp.source_sync.commands

import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
class SyncCheckCommand : AbstractCommand(
    "syncCheck",
    "Checks that downloaded sources were not modified on the installation since they were fetched."
) {
    override fun execute() {
        println("Executing syncCheck command...")
        val diff = getService().syncCheck(scripts, modules)
        if (diff.scripts.isNotEmpty() || diff.modules.isNotEmpty()) {
            println("Src not in sync:")
            if (diff.scripts.isNotEmpty()) {
                println("Scripts changed:")
                diff.scripts.forEach { println("code : ${it.code}, checksum: ${it.checksum}") }
            }
            if (diff.modules.isNotEmpty()) {
                println("Modules changed:")
                diff.modules.forEach { println("code : ${it.code}, checksum: ${it.checksum}") }
            }
        } else {
            println("All src in sync")
        }
    }
}
