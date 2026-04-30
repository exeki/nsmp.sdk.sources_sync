package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot

class EmptyRemoteSrcFileLookupRootException : RemoteSrcLookupException(MSG) {
    companion object {

        const val MSG = "Cant found any remote src files"

        fun throwIfNecessary(lookupResult: SrcLookupRoot<*>) {
            if (lookupResult.all { it.value.found.isEmpty() }) throw EmptyRemoteSrcFileLookupRootException()
        }
    }
}
