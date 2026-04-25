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
        logger.lifecycle("Running Pull task")
        logTaskOptionsSafely()
        val req = createRequest()
        val res = createService().pull(req)
        logger.lifecycle("Pulled ${res.scripts.size} scripts, ${res.modules.size} modules, ${res.advImports.size} advImports")
        if(res.scripts.isNotEmpty()) {
            logger.info("scripts:")
            res.scripts.forEach {
                logger.info(it.code)
            }
        }
        if(res.modules.isNotEmpty()) {
            logger.info("modules:")
            res.modules.forEach {
                logger.info(it.code)
            }
        }
        if(res.advImports.isNotEmpty()) {
            logger.info("advImports:")
            res.advImports.forEach {
                logger.info(it.code)
            }
        }
    }
}
