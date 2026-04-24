package ru.kazantsev.nsmp.source_sync.cli

import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
class SyncCheckCommand : AbstractCommand(
    "syncCheck",
    "Checks that downloaded sources were not modified on the installation since they were fetched."
) {
    override fun execute() {
        println("Executing syncCheck command...")
        val diff = getService().syncCheck(createRequest())
        if (diff.scripts.isNotEmpty() || diff.modules.isNotEmpty() || diff.advImports.isNotEmpty()) {
            println("Src not in sync:")
            if (diff.scripts.isNotEmpty()) {
                println("Scripts changed:")
                diff.scripts.forEach { println(it.code) }
            }
            if (diff.modules.isNotEmpty()) {
                println("Modules changed:")
                diff.modules.forEach {  println(it.code) }
            }
            if (diff.advImports.isNotEmpty()) {
                println("Adv imports changed:")
                diff.advImports.forEach {  println(it.code) }
            }
        } else println("All src in sync")
    }
}
