package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.AbstractTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.PullTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.PushTask
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.SyncCheckTask

@Suppress("unused")
class Plugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")

        val extension = project.extensions.create("nsmpSdkSourcesSync", Extension::class.java)
        val providers = project.providers

        configureSourceSets(project, extension)

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

private fun configureSourceSets(project: Project, extension: Extension) {
    val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
    val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
    val srcFoldersParams = extension.srcFoldersParams ?: SrcFoldersParams(project.projectDir.path)
    main.java.srcDir(srcFoldersParams.getModulesRelativePathString())
    main.java.srcDir(srcFoldersParams.getScriptsRelativePathString())
    main.resources.srcDir(srcFoldersParams.getAdvImportsRelativePathString())
}

private fun <T : AbstractTask> TaskProvider<T>.configureRemote(
    extension: Extension,
    providers: ProviderFactory,
    additional: T.() -> Unit = {}
): TaskProvider<T> {
    configure {
        it.doNotTrackState("This task must always run")
        it.connectorParamsProvider = providers.provider { extension.installation?.connectorParams }
        it.srcFoldersParamsProvider = providers.provider { extension.srcFoldersParams }
        it.additional()
    }
    return this
}
