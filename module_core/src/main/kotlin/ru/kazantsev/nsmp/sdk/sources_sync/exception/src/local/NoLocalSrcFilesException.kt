package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.signature.LookupThrowableIfNecessary

class NoLocalSrcFilesException() : LocalSrcException("Cant found any local src files") {
    companion object : LookupThrowableIfNecessary {
        override fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>) {
            if(lookupResult.isEmpty()) throw NoLocalSrcFilesException()
        }
    }
}