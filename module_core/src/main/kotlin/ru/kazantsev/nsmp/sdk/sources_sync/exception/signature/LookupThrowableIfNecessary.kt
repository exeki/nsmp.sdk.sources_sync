package ru.kazantsev.nsmp.sdk.sources_sync.exception.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot

interface LookupThrowableIfNecessary {
    fun throwIfNecessary(lookupResult: SrcLookupResultRoot<*>)
}