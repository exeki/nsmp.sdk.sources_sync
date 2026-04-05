package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.gradle.api.tasks.TaskAction

abstract class SyncCheckTask : AbstractTask() {

    companion object {
        const val NAME = "syncCheck"
    }

    init {
        description = "Checks remote src checksums against local checksums. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val requestedScripts = parseCsvOption(scripts.orNull)
        val requestedModules = parseCsvOption(modules.orNull)
        val srcService = createService()

        val diff = srcService.syncCheck(
            requestedScripts,
            requestedModules
        )

        if (diff.scripts.isEmpty() && diff.modules.isEmpty()) {
            logger.lifecycle("No src checksum differences found")
            return
        }

        diff.scripts.forEach { logger.lifecycle("Changed script: {}", it.code) }
        diff.modules.forEach { logger.lifecycle("Changed module: {}", it.code) }
    }
}
