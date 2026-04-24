package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder

class LocalSrcFilesService(srcFoldersParams: SrcFoldersParams) {
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

    fun findLocalFiles(req: SrcRequest): SrcLookupResultRoot<LocalFile> {
        return SrcLookupResultRoot(
            scripts = scriptsSrcFolder.findSourceFiles(req.getScriptsRequest()),
            modules = modulesSrcFolder.findSourceFiles(req.getModulesRequest()),
            advImports = advImportsSrcFolder.findSourceFiles(req.getAdvImportsRequest())
        )
    }

    fun whiteLocalFiles(src: SrcSetRoot<RemoteSrcTextInfo>): SrcSetRoot<LocalFileInfo> {
        return src.convert(
            scriptTransform = { srcText -> scriptsSrcFolder.writeSourceFile(srcText) },
            moduleTransform = { srcText -> scriptsSrcFolder.writeSourceFile(srcText) },
            advImportTransform = { srcText -> scriptsSrcFolder.writeSourceFile(srcText) }
        )
    }
}