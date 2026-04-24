package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

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

    val localSrcFileService = LocalSrcFilesService(srcFoldersParams)
    val localSrcInfoService = LocalSrcInfoService(srcFoldersParams.getProjectAbsolutePath())

    fun getLocalSrc(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
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
        return filesLookupResult.convertToSrcSetRoot(
            scriptConvertor = { srcFile -> link(srcFile, info.scripts) },
            moduleConvertor = { srcFile -> link(srcFile, info.modules) },
            advImportConvertor = { srcFile -> link(srcFile, info.advImports) }
        )
    }

    fun updateInfoFile(scriptChecksums: ScriptChecksums) {
        val root = SrcSetRoot(
            scripts = scriptChecksums.scripts.map { LocalInfo(it) }.toSet(),
            modules = scriptChecksums.modules.map { LocalInfo(it) }.toSet(),
            advImports = scriptChecksums.advimports.map { LocalInfo(it) }.toSet()
        )
        localSrcInfoService.updateInfoFile(root)
    }

    fun updateInfoFile(root: SrcSetRoot<LocalInfo>) {
        localSrcInfoService.updateInfoFile(root)
    }

    fun whiteLocalSrc(root: SrcSetRoot<RemoteSrcTextInfo>): SrcSetRoot<LocalFileInfo> {
        localSrcInfoService.updateInfoFile(root.convert { LocalInfo(it.info) })
        return localSrcFileService.whiteLocalFiles(root)
    }
}