package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion

class NotFoundLocalSrcFileLookupException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val lookupResult: SrcLookup<SrcFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        type = lookupResult.type,
        codes = lookupResult.notFound
    )
) {

    companion object : LookupResultExceptionCompanion() {

        const val MSG = "Some local src files not found:"

        fun throwIfNecessary(lookupResult: SrcLookup<SrcFile>) {
            if (lookupResult.notFound.isNotEmpty()) throw NotFoundLocalSrcFileLookupException(lookupResult)
        }
    }
}