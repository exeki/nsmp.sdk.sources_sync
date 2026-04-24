package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.ExceptionUtils

class LocalSrcFilesNotFoundException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val lookupResult: SrcLookupResultRoot<LocalFile>
) : LocalSrcException(
    ExceptionUtils.buildMessageForLookup(
        start = MSG,
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound
    )
) {

    companion object {

        const val MSG = "Some local src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<LocalFile>) {
            if (lookupResult.hasNotFound()) throw LocalSrcFilesNotFoundException(lookupResult)
        }
    }
}