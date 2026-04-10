package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.gradle.api.tasks.TaskAction

abstract class PullTask : AbstractTask() {

    companion object {
        const val NAME = "pull"
    }

    init {
        description = "Fetches sources from NSMP and stores them in project source sets. Use --scripts/--modules/--advImports or --allScripts/--allModules/--allAdvImports"
    }

    @TaskAction
    fun action() {
        createService().pull(createRequest())
    }
}
