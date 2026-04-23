package ru.kazantsev.nsmp.sdk.sources_sync

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.sync_check.SyncCheckResult
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptyPullResponse
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.NoLocalSrcFilesException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.SyncCheckFailedException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.LocalSrcFilesNotFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.DuplicatedLocalSrcFileFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.service.LocalSrcFilesService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcArchiveService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcChecksumService
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder
import ru.kazantsev.nsmp.sdk.sources_sync.service.LocalSrcInfoService
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
    val localSrcInfoService = LocalSrcInfoService(projectPath, objectMapper)
    val localSrcFileService = LocalSrcFilesService(scriptsSrcFolder, modulesSrcFolder, advImportsSrcFolder)
    val srcArchiveService = SrcArchiveService()

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(req: SrcRequest): SrcSetRoot<LocalSrcFileInfo> {

        log.info("Fetch started: req={}", req)
        val srcArchive = connector.getSrc(req)
        val root = srcArchiveService.unpackSrcArchive(srcArchive)
        if (root.isEmpty()) throw EmptyPullResponse()
        root.scripts.forEach { scriptsSrcFolder.writeSourceFile(it) }
        root.modules.forEach { modulesSrcFolder.writeSourceFile(it) }
        root.advImports.forEach { advImportsSrcFolder.writeSourceFile(it) }
        localSrcInfoService.updateInfoFile(root.convert { it.info })
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
    fun syncCheck(req: SrcRequest): SrcSetRoot<SyncCheckResult> {
        log.info("Diff started: {}", req)
        val remoteSrcInfo = getRemoteSrcInfo(req)

        val localSrcInfo = localSrcInfoService.readLocalSrcInfo(req)
        val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
        log.info(
            "Diff completed: changedScripts={}, changedModules={}, changedAdvImports={}",
            diff.scripts.size,
            diff.modules.size,
            diff.advImports.size
        )
    }

    /**
     * Собирает локальные исходники, проверяет их чексуммами и отправляет на сервер.
     */
    fun push(
        req: SrcRequest,
        force: Boolean = false
    ): SrcSetRoot<LocalSrcFileInfo> {
        log.info("Push started: req={}, force={}", req, force)

        val requestedRoot = SrcFileCodeDtoRoot(
            scripts = scriptsSrcFolder.findSourceFiles(req.scripts, req.allScripts, req.scriptCodesExcluded),
            modules = modulesSrcFolder.findSourceFiles(req.modules, req.allModules, req.modulesExcluded),
            advImports = advImportsSrcFolder.findSourceFiles(req.advImports, req.allAdvImports, req.advImportsExcluded)
        )

        if (requestedRoot.isEmpty()) throw NoLocalSrcFilesException("No local sources found to push")

        if (!force) {
            val remoteSrcInfo = getRemoteSrcInfo(req)
            val localSrcInfo = localSrcInfoService.readLocalSrcInfo(req)
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
        val result = SrcFileInfoDtoRoot(
            scripts = mapInfoToFile(pushedInfo.scripts, requestedRoot.scripts),
            modules = mapInfoToFile(pushedInfo.modules, requestedRoot.modules),
            advImports = mapInfoToFile(pushedInfo.advImports, requestedRoot.advImports)
        )
        localSrcInfoService.updateInfoFile(pushedInfo)
        log.info(
            "Push completed: scripts={}, modules={}, advImports={}",
            pushedInfo.scripts.size,
            pushedInfo.modules.size,
            pushedInfo.advImports.size
        )
        return result
    }

    private fun mapInfoToFile(infos: List<LocalSrcInfo>, files: List<LocalSrcFile>): List<LocalSrcFileInfo> {
        return infos.map { info ->
            val fileDto = files.find { it.code == info.code }
            if (fileDto == null) throw RuntimeException("Cannot map info ${info.code} with file")
            LocalSrcFileInfo(info = info, file = fileDto.file)
        }
    }

    private fun getLocalSrc(req: SrcRequest): SrcSetRoot<LocalSrcFileInfo> {
        val link = { localSrcFile: LocalSrcFile, info: Set<LocalSrcInfo> ->
            LocalSrcFileInfo(
                file = localSrcFile.file,
                code = localSrcFile.code,
                info = info.find { it.code == localSrcFile.code }
            )
        }
        val filesLookupResult = localSrcFileService.findLocalFiles(req)
        LocalSrcFilesNotFoundException.throwIfNecessary(filesLookupResult)
        NoLocalSrcFilesException.throwIfNecessary(filesLookupResult)
        DuplicatedLocalSrcFileFoundException.throwIfNecessary(filesLookupResult)
        val info = localSrcInfoService.readLocalSrcInfo(filesLookupResult.convertToRequest { it.code }).convert { it }
        return filesLookupResult.convert(
            scriptConvertor = { srcFile -> link(srcFile, info.scripts) },
            moduleConvertor = { srcFile -> link(srcFile, info.modules) },
            advImportConvertor = { srcFile -> link(srcFile, info.advImports) }
        )
    }

    /**
     * Получает с сервера актуальную информацию о чексуммах исходников.
     */
    private fun getRemoteSrcInfo(req: SrcRequest): ScriptChecksums {
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
