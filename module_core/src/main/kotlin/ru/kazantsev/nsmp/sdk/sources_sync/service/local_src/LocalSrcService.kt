package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.DuplicatedLocalSrcFileFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.LocalSrcFilesNotFoundException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.NoLocalSrcFilesException

class LocalSrcService(srcFoldersParams: SrcFoldersParams) {
    private val log = LoggerFactory.getLogger(javaClass)

    val localSrcFileService = LocalSrcFilesService(srcFoldersParams)
    val localSrcInfoService = LocalSrcInfoService(srcFoldersParams.getProjectAbsolutePath())

    fun getLocalSrc(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
        log.info("Read local src started: {}", req)
        val link = { localSrcFile: LocalFile, info: Set<LocalInfo> ->
            LocalFileInfo(
                file = localSrcFile.file,
                code = localSrcFile.code,
                info = info.find { it.code == localSrcFile.code }
            )
        }
        val filesLookupResult = localSrcFileService.findLocalFiles(req)
        LocalSrcFilesNotFoundException.throwIfNecessary(filesLookupResult)
        NoLocalSrcFilesException.throwIfNecessary(filesLookupResult)
        DuplicatedLocalSrcFileFoundException.throwIfNecessary(filesLookupResult)
        val info = localSrcInfoService.readLocalSrcInfo(filesLookupResult.convertToRequest { it.code })
            .convertToSrcSetRoot { it }
        val result = filesLookupResult.convertToSrcSetRoot(
            scriptConvertor = { srcFile -> link(srcFile, info.scripts) },
            moduleConvertor = { srcFile -> link(srcFile, info.modules) },
            advImportConvertor = { srcFile -> link(srcFile, info.advImports) }
        )
        log.info(
            "Read local src completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    fun updateInfoFile(scriptChecksums: ScriptChecksums) {
        log.info(
            "Update local info from remote checksums: scripts={}, modules={}, advImports={}",
            scriptChecksums.scripts.size,
            scriptChecksums.modules.size,
            scriptChecksums.advimports.size
        )
        val root = SrcSetRoot(
            scripts = scriptChecksums.scripts.map { LocalInfo(it) }.toSet(),
            modules = scriptChecksums.modules.map { LocalInfo(it) }.toSet(),
            advImports = scriptChecksums.advimports.map { LocalInfo(it) }.toSet()
        )
        localSrcInfoService.updateInfoFile(root)
    }

    fun updateInfoFile(root: SrcSetRoot<LocalInfo>) {
        log.debug(
            "Update local info requested: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        localSrcInfoService.updateInfoFile(root)
    }

    fun whiteLocalSrc(root: SrcSetRoot<RemoteSrcTextInfo>): SrcSetRoot<LocalFileInfo> {
        log.info(
            "Write local src started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        localSrcInfoService.updateInfoFile(root.convert { LocalInfo(it.info) })
        val result = localSrcFileService.whiteLocalFiles(root)
        log.info(
            "Write local src completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }
}
