package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.SyncCheckTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.PullTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.AbstractTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.PushTask
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService

class Plugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")

        val extension = project.extensions.create("nsmpSdkSourcesSync", Extension::class.java)
        val providers = project.providers

        configureSourceSets(project)

        project.tasks.register(
            PullTask.NAME,
            PullTask::class.java
        ).configureRemote(extension, providers)

        project.tasks.register(
            SyncCheckTask.NAME,
            SyncCheckTask::class.java
        ).configureRemote(extension, providers)

        project.tasks.register(
            PushTask.NAME,
            PushTask::class.java
        ).configureRemote(extension, providers)
    }
}

private fun configureSourceSets(project: Project) {
    val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
    val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
    main.java.srcDir(SrcService.DEFAULT_MODULES_PATH)
    main.java.srcDir(SrcService.DEFAULT_SCRIPTS_PATH)
    main.resources.srcDir(SrcService.DEFAULT_ADV_IMPORTS_PATH)
}

private fun <T : AbstractTask> TaskProvider<T>.configureRemote(
    extension: Extension,
    providers: ProviderFactory,
    additional: T.() -> Unit = {}
): TaskProvider<T> {
    configure {
        it.doNotTrackState("This task must always run")
        it.connectorParamsProvider = providers.provider { extension.installation?.connectorParams }
        it.additional()
    }
    return this
}
