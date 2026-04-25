package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.DuplicatedLocalSrcFileFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.LocalSrcFilesNotFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.NoLocalSrcFilesException

class LocalSrcService(srcFoldersParams: SrcFoldersParams) {
    private val log = LoggerFactory.getLogger(javaClass)

    val localSrcFileService = LocalSrcFileService(srcFoldersParams)
    val localSrcInfoService = LocalSrcInfoService(srcFoldersParams.getProjectAbsolutePath())

    fun <T : ILocalFile> compareLocalFileAndInfo(
        fileRoot: SrcSetRoot<T>,
        infoRoot: SrcSetRoot<LocalInfo>
    ): SrcSetRoot<LocalFileInfo> {
        val link = { localSrcFile: ILocalFile, info: SrcSet<LocalInfo> ->
            LocalFileInfo(
                file = localSrcFile.file,
                code = localSrcFile.code,
                info = info.getByCode(localSrcFile.code)
            )
        }
        return fileRoot.convert(
            scriptTransform = { srcFile: ILocalFile -> link(srcFile, infoRoot.scripts) },
            moduleTransform = { srcFile: ILocalFile -> link(srcFile, infoRoot.modules) },
            advImportTransform = { srcFile: ILocalFile -> link(srcFile, infoRoot.advImports) }
        )
    }

    fun getLocalSrc(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
        log.info("Read local src started: {}", req)
        val filesLookupResult = localSrcFileService.lookupLocalFiles(req)
        LocalSrcFilesNotFoundException.throwIfNecessary(filesLookupResult)
        NoLocalSrcFilesException.throwIfNecessary(filesLookupResult)
        DuplicatedLocalSrcFileFoundException.throwIfNecessary(filesLookupResult)
        val result = compareLocalFileAndInfo(
            fileRoot = filesLookupResult.convertToSrcSetRoot(),
            infoRoot = localSrcInfoService.getLocalSrcInfo(filesLookupResult.convertToRequest { it.code })
        )
        log.info(
            "Read local src completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    fun updateInfoFile(scriptChecksums: ScriptChecksums): SrcSetRoot<LocalInfo> {
        val root = SrcSetRoot(
            scripts = scriptChecksums.scripts.map { LocalInfo(it) }.toSet(),
            modules = scriptChecksums.modules.map { LocalInfo(it) }.toSet(),
            advImports = scriptChecksums.advimports.map { LocalInfo(it) }.toSet()
        )
        return updateInfoFile(root)
    }

    fun updateInfoFile(root: SrcSetRoot<LocalInfo>): SrcSetRoot<LocalInfo> {
        log.debug(
            "Update local info requested: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        return localSrcInfoService.updateInfoFile(root)
    }

    fun whiteLocalSrc(root: SrcSetRoot<RemoteSrcTextInfo>): SrcSetRoot<LocalFileInfo> {
        log.info(
            "Write local src started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        updateInfoFile(root.convert { LocalInfo(it.info) })
        val result = localSrcFileService.whiteLocalFiles(root)
        log.info("Write local src completed")
        return result
    }
}
