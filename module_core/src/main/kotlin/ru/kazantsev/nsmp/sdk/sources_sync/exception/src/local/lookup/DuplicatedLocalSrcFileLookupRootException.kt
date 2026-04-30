package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultRootExceptionCompanion

class DuplicatedLocalSrcFileLookupRootException(
    @Suppress("unused")
    val lookupResultRoot: SrcLookupRoot<SrcFile>
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

        fun throwIfNecessary(lookupResultRoot: SrcLookupRoot<SrcFile>) {
            if (lookupResultRoot.hasDuplicates()) throw DuplicatedLocalSrcFileLookupRootException(lookupResultRoot)
        }
    }
}