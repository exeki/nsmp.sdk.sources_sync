package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class DuplicatedLocalSrcFileLookupResultRootException(
    @Suppress("unused")
    val lookupResultRoot: SrcLookupResultRoot<LocalFile>
) : LocalSrcLookupException(
    buildMessage(
        start = MSG,
        scripts = lookupResultRoot.scripts.duplicated,
        modules = lookupResultRoot.scripts.duplicated,
        advImports = lookupResultRoot.scripts.duplicated
    )
) {

    companion object : LookupResultRootExceptionCompanion() {
        const val MSG = "Some local src files duplicated:"

        fun throwIfNecessary(srcLookupResultRoot: SrcLookupResultRoot<LocalFile>) {
            if (srcLookupResultRoot.hasDuplicates()) throw DuplicatedLocalSrcFileLookupResultRootException(srcLookupResultRoot)
        }
    }
}