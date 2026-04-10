package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.gradle.api.tasks.TaskAction

abstract class SyncCheckTask : AbstractTask() {

    companion object {
        const val NAME = "syncCheck"
    }

    init {
        description = "Checks remote src checksums against local checksums. Use --scripts/--modules/--advImports or --allScripts/--allModules/--allAdvImports"
    }

    @TaskAction
    fun action() {
        val diff = createService().syncCheck(createRequest())

        if (diff.scripts.isEmpty() && diff.modules.isEmpty() && diff.advImports.isEmpty()) {
            logger.lifecycle("No src checksum differences found")
            return
        }

        diff.scripts.forEach { logger.lifecycle("Changed script: {}", it.code) }
        diff.modules.forEach { logger.lifecycle("Changed module: {}", it.code) }
        diff.advImports.forEach { logger.lifecycle("Changed adv import: {}", it.code) }
    }
}
