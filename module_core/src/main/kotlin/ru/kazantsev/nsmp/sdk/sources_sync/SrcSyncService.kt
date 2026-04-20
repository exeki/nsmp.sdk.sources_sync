package ru.kazantsev.nsmp.sdk.sources_sync

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.exception.NoSrcException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.SyncCheckFailedException
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcArchiveService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcChecksumService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcStorageService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcSyncConnector
import java.nio.file.Path

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcSyncService(
    connectorParams: ConnectorParams,
    objectMapper: ObjectMapper,
    srcFoldersParams: SrcFoldersParams
) {

    private val connector = SrcSyncConnector(connectorParams)
    private val log = LoggerFactory.getLogger(javaClass)

    val projectPath: Path = Path.of(srcFoldersParams.getProjectAbsolutePathString())
    val srcChecksumService = SrcChecksumService()
    val scriptsSrcFolder = SrcFolder(projectPath, srcFoldersParams.getScriptsRelativePathString(), "groovy")
    val modulesSrcFolder = SrcFolder(projectPath, srcFoldersParams.getModulesRelativePathString(), "groovy")
    val advImportsSrcFolder = SrcFolder(projectPath, srcFoldersParams.getAdvImportsRelativePathString(), "xml")
    val srcStorageService = SrcStorageService(projectPath, objectMapper)
    val srcArchiveService = SrcArchiveService(scriptsSrcFolder, modulesSrcFolder, advImportsSrcFolder)

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(req: SrcRequest): SrcDtoRoot {
        log.info("Fetch started: req={}", req)
        val srcArchive = connector.getSrc(req)
        val root = srcArchiveService.unpackSrcArchive(srcArchive)
        if (root.isEmpty()) throw NoSrcException("No remote source files found")
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

        if (requestedRoot.isEmpty()) throw NoSrcException("No local sources found to push")

        if (!force) {
            val remoteSrcInfo = getRemoteSrcInfo(req)
            val localSrcInfo = srcStorageService.readLocalSrcInfo(req)
            if (!localSrcInfo.isEmpty()) {
                val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
                if (!diff.isEmpty()) throw SyncCheckFailedException(
                    buildString {
                        append("Src check failed. Changed scripts=")
                        append(diff.scripts.map { it.code })
                        append(", changed modules=")
                        append(diff.modules.map { it.code })
                        append(", changed adv imports=")
                        append(diff.advImports.map { it.code })
                    },
                    diff
                )
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
