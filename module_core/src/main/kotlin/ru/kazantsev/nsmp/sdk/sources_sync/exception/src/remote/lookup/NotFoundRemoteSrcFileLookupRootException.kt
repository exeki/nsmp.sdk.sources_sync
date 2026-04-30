package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class NotFoundRemoteSrcFileLookupRootException(
    @Suppress("unused")
    val lookupResult: SrcLookupRoot<*>
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

        fun throwIfNecessary(lookupResult: SrcLookupRoot<*>) {
            if (lookupResult.hasNotFound()) throw NotFoundRemoteSrcFileLookupRootException(lookupResult)
        }
    }
}