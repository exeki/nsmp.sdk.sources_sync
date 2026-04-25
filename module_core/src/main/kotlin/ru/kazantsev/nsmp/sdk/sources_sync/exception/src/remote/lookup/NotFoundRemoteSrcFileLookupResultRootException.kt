package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup.RemoteSrcLookupException

class NotFoundRemoteSrcFileLookupResultRootException(
    @Suppress("unused")
    val lookupResult: SrcLookupResultRoot<*>
) : RemoteSrcLookupException(
    buildMessage(
        start = MSG,
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound
    )
) {

    companion object : LookupResultRootExceptionCompanion() {

        const val MSG = "Some remote src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if (lookupResult.hasNotFound()) throw NotFoundRemoteSrcFileLookupResultRootException(lookupResult)
        }
    }
}