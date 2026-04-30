package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class NotFoundLocalSrcFileLookupRootException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val lookupResult: SrcLookupRoot<SrcFile>
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

        fun throwIfNecessary(lookupResultRoot: SrcLookupRoot<SrcFile>) {
            if (lookupResultRoot.hasNotFound()) throw NotFoundLocalSrcFileLookupRootException(lookupResultRoot)
        }
    }
}