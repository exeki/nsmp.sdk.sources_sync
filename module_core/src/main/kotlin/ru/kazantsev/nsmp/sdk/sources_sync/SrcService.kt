package ru.kazantsev.nsmp.sdk.sources_sync

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDtoRoot
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

    val srcChecksumService = SrcChecksumService()
    val scriptsSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_SCRIPTS_PATH), "groovy")
    val modulesSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_MODULES_PATH), "groovy")
    val advImportsSrcFolder = SrcFolder(projectPath.resolve(DEFAULT_ADV_IMPORTS_PATH), "xml")
    val srcStorageService = SrcStorageService(projectPath, objectMapper)
    val srcArchiveService = SrcArchiveService(
        scriptsSrcFolder,
        modulesSrcFolder,
        advImportsSrcFolder
    )

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(req: SrcRequest): SrcDtoRoot {
        log.info("Fetch started: req={}", req)
        val srcArchive = connector.getSrc(req)
        val root = srcArchiveService.unpackSrcArchive(srcArchive)
        if (root.scripts.isEmpty() && root.advImports.isEmpty() && root.modules.isEmpty()) throw RuntimeException("No source files found")
        root.scripts.forEach { scriptsSrcFolder.writeSourceFile(it) }
        root.modules.forEach { modulesSrcFolder.writeSourceFile(it) }
        root.advImports.forEach { advImportsSrcFolder.writeSourceFile(it) }
        srcStorageService.updateInfoFile(root.toInfo())
        log.info(
            "Fetch completed: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        return root
    }

    /**
     * Получает чексуммы с сервера и сравнивает их с локальным хранилищем.
     */
    fun syncCheck(req: SrcRequest): SrcInfoRoot {
        log.info("Diff started: {}", req)
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

        val requestedRoot = SrcFileDtoRoot(
            scripts = scriptsSrcFolder.findSourceFiles(
                req.scripts,
                req.allScripts,
                req.scriptsExcluded
            ),
            modules = modulesSrcFolder.findSourceFiles(
                req.modules,
                req.allModules,
                req.modulesExcluded
            ),
            advImports = advImportsSrcFolder.findSourceFiles(
                req.advImports,
                req.allAdvImports,
                req.advImportsExcluded
            )
        )

        if (requestedRoot.scripts.isEmpty() && requestedRoot.modules.isEmpty() && requestedRoot.advImports.isEmpty()) throw IllegalStateException(
            "No sources found to upload"
        )

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
        val scriptsChecksums = connector.pushScripts(srcArchiveService.buildSrcArchive(requestedRoot))
        val pushedInfo = SrcInfoRoot.fromChecksums(scriptsChecksums)
        srcStorageService.updateInfoFile(pushedInfo)
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
