package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion

class NotFoundLocalSrcFileLookupResultException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val lookupResult: SrcLookupResult<LocalFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        type = lookupResult.type,
        codes = lookupResult.notFound
    )
) {

    companion object : LookupResultExceptionCompanion() {

        const val MSG = "Some local src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookupResult<LocalFile>) {
            if (lookupResult.notFound.isNotEmpty()) throw NotFoundLocalSrcFileLookupResultException(lookupResult)
        }
    }
}