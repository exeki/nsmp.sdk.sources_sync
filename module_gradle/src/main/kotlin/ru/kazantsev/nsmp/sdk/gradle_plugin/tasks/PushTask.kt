package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class PushTask : AbstractTask() {

    companion object {
        const val NAME = "push"
    }

    @get:Input
    @get:Optional
    @get:Option(option = "force", description = "Skip src checksum validation")
    abstract val force: Property<String>

    init {
        description = "Pushes local sources to NSMP. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val srcService = createService()
        srcService.push(
            parseCsvOption(scripts.orNull),
            parseCsvOption(modules.orNull),
            parseBooleanOption("force", force.orNull ?: "false")
        )
    }
}
