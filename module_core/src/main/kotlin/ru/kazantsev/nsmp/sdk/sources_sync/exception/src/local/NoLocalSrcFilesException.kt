package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot

class NoLocalSrcFilesException() : LocalSrcException(MSG) {
    companion object  {
         fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if(lookupResult.isEmpty()) throw NoLocalSrcFilesException()
        }

        const val MSG = "Cant found any local src files"
    }
}