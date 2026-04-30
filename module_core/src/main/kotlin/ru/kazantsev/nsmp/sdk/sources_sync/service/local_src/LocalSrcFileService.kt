package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.Root
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcText
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.storage.SrcFolder

class LocalSrcFileService(srcFoldersParams: SrcFoldersParams) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val srcForderRoot = Root.byEnumIterator { type ->
        SrcFolder(
            projectPath = srcFoldersParams.getProjectAbsolutePath(),
            relativePathString = srcFoldersParams.getSrcRelativePathByType(type),
            type = type
        )
    }

    fun lookupLocalFileSrcSet(req: SrcSetRequest): SrcLookup<SrcFile> {
        return srcForderRoot[req.type].lookupLocalFiles(req)
    }

    fun lookupLocalFileSrcSetRoot(req: SrcRequest): SrcLookupRoot<SrcFile> {
        log.debug("Find local files started: {}", req)
        val result = SrcLookupRoot.byEnumIterator { srcForderRoot[it].lookupLocalFiles(req.getSetRequest(it)) }
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

    fun <T : ISrcText> whiteLocalFiles(src: SrcSetRoot<T>): SrcSetRoot<SrcFile> {
        log.debug(
            "Write local files started: scripts={}, modules={}, advImports={}",
            src.scripts.size,
            src.modules.size,
            src.advImports.size
        )
        val result = src.convert { type, srcText -> srcForderRoot[type].writeSourceFile(srcText) }
        log.debug(
            "Write local files completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }
}
