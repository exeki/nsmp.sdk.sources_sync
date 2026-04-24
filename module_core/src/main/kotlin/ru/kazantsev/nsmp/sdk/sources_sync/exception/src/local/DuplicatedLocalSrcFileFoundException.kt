package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.ExceptionUtils

class DuplicatedLocalSrcFileFoundException(
    @Suppress("unused")
    val lookupResult: SrcLookupResultRoot<LocalFile>
) : LocalSrcException(
    ExceptionUtils.buildMessageForLookup(
        start = MSG,
        scripts = lookupResult.scripts.duplicated,
        modules = lookupResult.modules.duplicated,
        advImports = lookupResult.advImports.duplicated
    )
) {

    companion object {
        const val MSG = "Some local src files duplicated:"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<LocalFile>) {
            if (lookupResult.hasDuplicates()) throw DuplicatedLocalSrcFileFoundException(lookupResult)
        }
    }
}
