package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class NotFoundLocalSrcFileLookupResultRootException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val lookupResult: SrcLookupResultRoot<LocalFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound
    )
) {

    companion object : LookupResultRootExceptionCompanion() {

        const val MSG = "Some local src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<LocalFile>) {
            if (lookupResult.hasNotFound()) throw NotFoundLocalSrcFileLookupResultRootException(lookupResult)
        }
    }
}