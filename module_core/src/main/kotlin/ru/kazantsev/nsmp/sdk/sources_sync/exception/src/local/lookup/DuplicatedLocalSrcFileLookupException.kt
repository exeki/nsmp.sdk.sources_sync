package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion

class DuplicatedLocalSrcFileLookupException(
    @Suppress("unused")
    val lookupResult: SrcLookup<SrcFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        type = lookupResult.type,
        codes = lookupResult.duplicated
    )
) {

    companion object : LookupResultExceptionCompanion(){
        const val MSG = "Some local src files duplicated:"

        fun throwIfNecessary(lookupResult: SrcLookup<SrcFile>) {
            if (lookupResult.duplicated.isNotEmpty()) throw DuplicatedLocalSrcFileLookupException(lookupResult)
        }
    }
}