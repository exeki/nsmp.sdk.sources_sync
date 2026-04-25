package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup.RemoteSrcLookupException

class EmptyRemoteSrcFileLookupResultRootException : RemoteSrcLookupException(MSG) {
    companion object {

        const val MSG = "Cant found any remote src files"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if (lookupResult.isEmpty()) throw EmptyRemoteSrcFileLookupResultRootException()
        }
    }
}
