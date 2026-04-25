package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion

class EmptyLocalSrcFileLookupResultException(
    @Suppress("unused")
    val srcLookupResult: SrcLookupResult<LocalFile>
) : LocalSrcLookupException(MSG) {
    companion object : LookupResultExceptionCompanion() {

        fun throwIfNecessary(srcLookupResult: SrcLookupResult<LocalFile>) {
            if (srcLookupResult.found.isEmpty()) throw EmptyLocalSrcFileLookupResultException(srcLookupResult)
        }

        const val MSG = "Cant found any local src files"
    }
}