package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

import org.gradle.api.tasks.TaskAction

abstract class PullTask : AbstractTask() {

    companion object {
        const val NAME = "pull"
    }

    init {
        description = "Fetches sources from NSMP and stores them in project source sets. Supports include/exclude CSV lists and --allScripts/--allModules/--allAdvImports"
    }

    @TaskAction
    fun action() {
        val req = createRequest()
        createService().pull(req)
    }
}
