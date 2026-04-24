package ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSet


class SrcLookupResult<T : ISrcCode>(
    val type: SrcType,
    val notFound: Set<String> = setOf(),
    val duplicated: Set<String> = setOf(),
    found: Set<T>
) {

    val found: SrcSet<T> = SrcSet(set = found, type = type)

    companion object {
        fun <T : ISrcCode> empty(type: SrcType): SrcLookupResult<T> {
            return SrcLookupResult(
                type = type,
                notFound = setOf(),
                duplicated = setOf(),
                found = emptySet()
            )
        }
    }
}