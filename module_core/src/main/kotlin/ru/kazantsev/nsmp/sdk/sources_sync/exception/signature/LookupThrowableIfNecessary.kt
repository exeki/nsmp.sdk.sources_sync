package ru.kazantsev.nsmp.sdk.sources_sync.exception.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResultRoot

interface LookupThrowableIfNecessary {
    fun throwIfNecessary(lookupResult: LookupResultRoot<*>)
}