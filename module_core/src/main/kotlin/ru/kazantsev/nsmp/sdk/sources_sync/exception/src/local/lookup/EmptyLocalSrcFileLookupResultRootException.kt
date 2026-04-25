package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class EmptyLocalSrcFileLookupResultRootException(
    @Suppress("unused")
    val srcLookupResultRoot: SrcLookupResultRoot<LocalFile>
) : LocalSrcLookupException(MSG) {
    companion object : LookupResultRootExceptionCompanion() {

        fun throwIfNecessary(srcLookupResultRoot: SrcLookupResultRoot<LocalFile>) {
            if (srcLookupResultRoot.isEmpty()) throw EmptyLocalSrcFileLookupResultRootException(srcLookupResultRoot)
        }

        const val MSG = "Cant found any local src files"
    }
}