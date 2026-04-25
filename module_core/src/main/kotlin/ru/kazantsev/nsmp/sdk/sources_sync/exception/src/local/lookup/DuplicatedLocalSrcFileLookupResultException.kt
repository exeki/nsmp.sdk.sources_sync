package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion

class DuplicatedLocalSrcFileLookupResultException(
    @Suppress("unused")
    val lookupResult: SrcLookupResult<LocalFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        type = lookupResult.type,
        codes = lookupResult.duplicated
    )
) {

    companion object : LookupResultExceptionCompanion(){
        const val MSG = "Some local src files duplicated:"

        fun throwIfNecessary(lookupResult: SrcLookupResult<LocalFile>) {
            if (lookupResult.duplicated.isNotEmpty()) throw DuplicatedLocalSrcFileLookupResultException(lookupResult)
        }
    }
}