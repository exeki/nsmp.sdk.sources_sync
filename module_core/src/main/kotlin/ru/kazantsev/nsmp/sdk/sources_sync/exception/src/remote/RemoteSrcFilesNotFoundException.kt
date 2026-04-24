package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.ExceptionUtils

class RemoteSrcFilesNotFoundException(
    @Suppress("unused")
    val lookupResult: SrcLookupResultRoot<*>
) : RemoteSrcException(
    ExceptionUtils.buildMessageForLookup(
        start = MSG,
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound
    )
) {

    companion object {

        const val MSG = "Some remote src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if (lookupResult.hasNotFound()) throw RemoteSrcFilesNotFoundException(lookupResult)
        }
    }
}