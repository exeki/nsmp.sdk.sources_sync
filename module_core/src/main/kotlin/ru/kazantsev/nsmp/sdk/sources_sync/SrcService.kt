package ru.kazantsev.nsmp.sdk.sources_sync

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcArchiveService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcChecksumService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcStorageService
import java.nio.file.Path

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcService(
    private val connector: SrcConnector,
    private val objectMapper: ObjectMapper,
    private val projectPath: Path
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val DEFAULT_SCRIPTS_PATH: String = "src\\main\\scripts"
        const val DEFAULT_MODULES_PATH: String = "src\\main\\modules"
    }

    val srcStorageService = SrcStorageService(projectPath, objectMapper)
    val srcArchiveService = SrcArchiveService(objectMapper)
    val srcChecksumService = SrcChecksumService()
    val scriptsSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_SCRIPTS_PATH))
    val modulesSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_MODULES_PATH))

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(scripts: List<String>, modules: List<String>): SrcDtoRoot {
        log.info("Fetch started: scripts={}, modules={}", scripts.size, modules.size)
        if (scripts.isEmpty() && modules.isEmpty()) throw IllegalArgumentException("Sources must be specified. Please specify modules or scripts")
        val srcArchive = connector.getSrc(scripts, modules)
        val srcRoot = srcArchiveService.unpackSrcArchive(srcArchive)
        srcRoot.scripts.forEach { scriptsSrcFolder.writeSourceFile(it) }
        srcRoot.modules.forEach { modulesSrcFolder.writeSourceFile(it) }
        srcStorageService.updateInfoFile(srcRoot.scripts.map { it.info }, srcRoot.modules.map { it.info })
        log.info("Fetch completed: scripts={}, modules={}", srcRoot.scripts.size, srcRoot.modules.size)
        return srcRoot
    }

    /**
     * Получает чексуммы с сервера и сравнивает их с локальным хранилищем.
     */
    fun syncCheck(
        scripts: List<String>,
        modules: List<String>
    ): SrcInfoRoot {
        log.info("Diff started: scriptsFilter={}, modulesFilter={}", scripts.size, modules.size)
        val effectiveScripts =
            scripts.ifEmpty { scriptsSrcFolder.getAllSourceFiles().map { it.code } }
        val effectiveModules =
            modules.ifEmpty { modulesSrcFolder.getAllSourceFiles().map { it.code } }
        if (effectiveModules.isEmpty() && effectiveScripts.isEmpty()) throw IllegalStateException("No sources found to sync check")
        val remoteSrcInfo = getRemoteSrcInfo(effectiveScripts, effectiveModules)
        val localSrcInfo = srcStorageService.readLocalSrcInfo(scripts, modules)
        val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
        log.info("Diff completed: changedScripts={}, changedModules={}", diff.scripts.size, diff.modules.size)
        return diff
    }

    /**
     * Собирает локальные исходники, проверяет их чексуммами и отправляет на сервер.
     */
    fun push(
        scripts: List<String>,
        modules: List<String>,
        force: Boolean = false
    ): SrcInfoRoot {
        log.info("Push started: scriptsFilter={}, modulesFilter={}, force={}", scripts.size, modules.size, force)
        val requestedScripts: List<SrcFileDto> =
            if (scripts.isEmpty() && modules.isEmpty()) scriptsSrcFolder.getAllSourceFiles()
            else scriptsSrcFolder.findSourceFiles(scripts)
        val requestedModules: List<SrcFileDto> =
            if (scripts.isEmpty() && modules.isEmpty()) modulesSrcFolder.getAllSourceFiles()
            else modulesSrcFolder.findSourceFiles(modules)
        if (requestedScripts.isEmpty() && requestedModules.isEmpty()) throw IllegalStateException("No sources found to upload")
        if (!force) {
            val requestedScriptCodes = requestedScripts.map { it.code }
            val requestedModuleCodes = requestedModules.map { it.code }
            val remoteSrcInfo = getRemoteSrcInfo(requestedScriptCodes, requestedModuleCodes)
            val localSrcInfo = srcStorageService.readLocalSrcInfo(requestedScriptCodes, requestedModuleCodes)
            if (localSrcInfo.scripts.isNotEmpty() || localSrcInfo.modules.isNotEmpty()) {
                val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
                if (diff.scripts.isNotEmpty() || diff.modules.isNotEmpty()) {
                    throw IllegalStateException(
                        buildString {
                            append("Src check failed. Changed scripts=")
                            append(diff.scripts.map { it.code })
                            append(", changed modules=")
                            append(diff.modules.map { it.code })
                        }
                    )
                }
            }
        }
        val srcArchive = srcArchiveService.buildSrcArchive(
            requestedScripts,
            requestedModules,
            scriptsSrcFolder,
            modulesSrcFolder
        )
        val pushedSourcesInfo = connector.pushScripts(srcArchive)
        val pushedInfo = SrcInfoRoot(
            scripts = pushedSourcesInfo.scripts.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
            modules = pushedSourcesInfo.modules.map { SrcInfo(it.checksum, it.code) }.toMutableList()
        )
        srcStorageService.updateInfoFile(
            pushedInfo.scripts,
            pushedInfo.modules
        )
        log.info("Push completed: scripts={}, modules={}", pushedInfo.scripts.size, pushedInfo.modules.size)
        return pushedInfo
    }

    /**
     * Получает с сервера актуальную информацию о чексуммах исходников.
     */
    private fun getRemoteSrcInfo(scripts: List<String>, modules: List<String>): SrcInfoRoot {
        log.info("Remote info request started: scripts={}, modules={}", scripts.size, modules.size)
        val remoteInfo = connector.getSrcInfo(scripts, modules)
        log.info("Remote info request completed: scripts={}, modules={}", remoteInfo.scripts.size, remoteInfo.modules.size)
        return remoteInfo
    }
}
