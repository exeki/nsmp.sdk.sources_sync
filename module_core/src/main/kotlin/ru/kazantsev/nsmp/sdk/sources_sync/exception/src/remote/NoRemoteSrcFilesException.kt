package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot

class NoRemoteSrcFilesException : RemoteSrcException(MSG) {
    companion object {

        const val MSG = "Cant found any remote src files"

        fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if (lookupResult.isEmpty()) throw NoRemoteSrcFilesException()
        }
    }
}
