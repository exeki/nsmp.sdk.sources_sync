package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder

class LocalSrcFilesService(srcFoldersParams: SrcFoldersParams) {
    private val log = LoggerFactory.getLogger(javaClass)

    val scriptsSrcFolder = SrcFolder(
        projectPath = srcFoldersParams.getProjectAbsolutePath(),
        relativePathString = srcFoldersParams.getScriptsRelativePathString(),
        type = SrcType.SCRIPT
    )
    val modulesSrcFolder = SrcFolder(
        projectPath = srcFoldersParams.getProjectAbsolutePath(),
        relativePathString = srcFoldersParams.getModulesRelativePathString(),
        type = SrcType.MODULE
    )
    val advImportsSrcFolder = SrcFolder(
        projectPath = srcFoldersParams.getProjectAbsolutePath(),
        relativePathString = srcFoldersParams.getAdvImportsRelativePathString(),
        type = SrcType.ADV_IMPORT
    )

    fun lookupLocalFiles(req: SrcRequest): SrcLookupResultRoot<LocalFile> {
        log.debug("Find local files started: {}", req)
        val result = SrcLookupResultRoot(
            scripts = scriptsSrcFolder.findSourceFiles(req.getScriptsRequest()),
            modules = modulesSrcFolder.findSourceFiles(req.getModulesRequest()),
            advImports = advImportsSrcFolder.findSourceFiles(req.getAdvImportsRequest())
        )
        log.debug(
            "Find local files completed: scripts(found={}, notFound={}, duplicated={}), modules(found={}, notFound={}, duplicated={}), advImports(found={}, notFound={}, duplicated={})",
            result.scripts.found.size,
            result.scripts.notFound.size,
            result.scripts.duplicated.size,
            result.modules.found.size,
            result.modules.notFound.size,
            result.modules.duplicated.size,
            result.advImports.found.size,
            result.advImports.notFound.size,
            result.advImports.duplicated.size
        )
        return result
    }

    fun whiteLocalFiles(src: SrcSetRoot<RemoteSrcTextInfo>): SrcSetRoot<LocalFileInfo> {
        log.debug(
            "Write local files started: scripts={}, modules={}, advImports={}",
            src.scripts.size,
            src.modules.size,
            src.advImports.size
        )
        val result = src.convert(
            scriptTransform = { srcText -> scriptsSrcFolder.writeSourceFile(srcText) },
            moduleTransform = { srcText -> modulesSrcFolder.writeSourceFile(srcText) },
            advImportTransform = { srcText -> advImportsSrcFolder.writeSourceFile(srcText) }
        )
        log.debug(
            "Write local files completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }
}
