package ru.kazantsev.nsmp.sdk.sources_sync

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
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
        const val DEFAULT_ADV_IMPORTS_PATH: String = "src\\main\\resources"
    }

    val srcStorageService = SrcStorageService(projectPath, objectMapper)
    val srcArchiveService = SrcArchiveService(objectMapper)
    val srcChecksumService = SrcChecksumService()
    val scriptsSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_SCRIPTS_PATH), "groovy")
    val modulesSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_MODULES_PATH), "groovy")
    val advImportsSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_ADV_IMPORTS_PATH), "xml")

    private fun checkReqIsNotEmpty(req: SrcRequest) {
        if (!req.allModules && !req.allScripts && !req.allAdvImports && req.modules.isEmpty() && req.scripts.isEmpty() && req.advImports.isEmpty()) {
            throw IllegalArgumentException("Sources must be specified. Please specify modules, scripts or adv imports")
        }
    }

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(req: SrcRequest): SrcDtoRoot {
        log.info("Fetch started: req={}", req)
        checkReqIsNotEmpty(req)
        val srcArchive = connector.getSrc(req)
        val srcRoot = srcArchiveService.unpackSrcArchive(srcArchive)
        srcRoot.scripts.forEach { scriptsSrcFolder.writeSourceFile(it) }
        srcRoot.modules.forEach { modulesSrcFolder.writeSourceFile(it) }
        srcRoot.advImports.forEach { advImportsSrcFolder.writeSourceFile(it) }
        srcStorageService.updateInfoFile(
            srcRoot.scripts.map { it.info },
            srcRoot.modules.map { it.info },
            srcRoot.advImports.map { it.info }
        )
        log.info(
            "Fetch completed: scripts={}, modules={}, advImports={}",
            srcRoot.scripts.size,
            srcRoot.modules.size,
            srcRoot.advImports.size
        )
        return srcRoot
    }

    /**
     * Получает чексуммы с сервера и сравнивает их с локальным хранилищем.
     */
    fun syncCheck(req: SrcRequest): SrcInfoRoot {
        log.info("Diff started: {}", req)
        checkReqIsNotEmpty(req)
        val remoteSrcInfo = getRemoteSrcInfo(req)
        val localSrcInfo = srcStorageService.readLocalSrcInfo(req)
        val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
        log.info(
            "Diff completed: changedScripts={}, changedModules={}, changedAdvImports={}",
            diff.scripts.size,
            diff.modules.size,
            diff.advImports.size
        )
        return diff
    }

    /**
     * Собирает локальные исходники, проверяет их чексуммами и отправляет на сервер.
     */
    fun push(
        req: SrcRequest,
        force: Boolean = false
    ): SrcInfoRoot {
        log.info("Push started: req={}, force={}", req, force)

        checkReqIsNotEmpty(req)

        val requestedScripts: List<SrcFileDto> = if (req.allScripts) scriptsSrcFolder.getAllSourceFiles()
        else scriptsSrcFolder.findSourceFiles(req.scripts)

        val requestedModules: List<SrcFileDto> = if (req.allModules) modulesSrcFolder.getAllSourceFiles()
        else modulesSrcFolder.findSourceFiles(req.modules)

        val requestedAdvImports: List<SrcFileDto> = if (req.allAdvImports) advImportsSrcFolder.getAllSourceFiles()
        else advImportsSrcFolder.findSourceFiles(req.advImports)

        if (requestedScripts.isEmpty() && requestedModules.isEmpty() && requestedAdvImports.isEmpty()) throw IllegalStateException("No sources found to upload")

        if (!force) {
            val remoteSrcInfo = getRemoteSrcInfo(req)
            val localSrcInfo = srcStorageService.readLocalSrcInfo(req)
            if (localSrcInfo.scripts.isNotEmpty() || localSrcInfo.modules.isNotEmpty() || localSrcInfo.advImports.isNotEmpty()) {
                val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
                if (diff.scripts.isNotEmpty() || diff.modules.isNotEmpty() || diff.advImports.isNotEmpty()) {
                    throw IllegalStateException(
                        buildString {
                            append("Src check failed. Changed scripts=")
                            append(diff.scripts.map { it.code })
                            append(", changed modules=")
                            append(diff.modules.map { it.code })
                            append(", changed adv imports=")
                            append(diff.advImports.map { it.code })
                        }
                    )
                }
            }
        } else log.warn("force push enabled!")
        val srcArchive = srcArchiveService.buildSrcArchive(
            requestedScripts,
            requestedModules,
            requestedAdvImports,
            scriptsSrcFolder,
            modulesSrcFolder,
            advImportsSrcFolder,
        )
        val pushedSourcesInfo = connector.pushScripts(srcArchive)
        val pushedInfo = SrcInfoRoot(
            scripts = pushedSourcesInfo.scripts.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
            modules = pushedSourcesInfo.modules.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
            advImports = pushedSourcesInfo.advimports.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
        )
        srcStorageService.updateInfoFile(
            pushedInfo.scripts,
            pushedInfo.modules,
            pushedInfo.advImports
        )
        log.info(
            "Push completed: scripts={}, modules={}, advImports={}",
            pushedInfo.scripts.size,
            pushedInfo.modules.size,
            pushedInfo.advImports.size
        )
        return pushedInfo
    }

    /**
     * Получает с сервера актуальную информацию о чексуммах исходников.
     */
    private fun getRemoteSrcInfo(req: SrcRequest): SrcInfoRoot {
        log.info("Remote info request started: {}", req)
        val remoteInfo = connector.getSrcInfo(req)
        log.info(
            "Remote info request completed: scripts={}, modules={}, advImports={}",
            remoteInfo.scripts.size,
            remoteInfo.modules.size,
            remoteInfo.advImports.size
        )
        return remoteInfo
    }
}
