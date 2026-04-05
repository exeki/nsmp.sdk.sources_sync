package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.gradle.api.tasks.TaskAction

abstract class PullTask : AbstractTask() {

    companion object {
        const val NAME = "pull"
    }

    init {
        description = "Fetches sources from NSMP and stores them in project source sets. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val requestedScripts = parseCsvOption(scripts.orNull)
        val requestedModules = parseCsvOption(modules.orNull)

        val srcService = createService()
        srcService.pull(
            requestedScripts,
            requestedModules
        )
    }
}
