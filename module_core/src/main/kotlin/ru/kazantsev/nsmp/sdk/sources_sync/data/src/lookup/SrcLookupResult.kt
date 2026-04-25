package ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet


class SrcLookupResult<T : ISrcCode>(
    val type: SrcType,
    val notFound: Set<String> = setOf(),
    val duplicated: Set<String> = setOf(),
    found: Set<T>
) {

    val found: SrcSet<T> = SrcSet(set = found, type = type)

    fun convertToSrcSet() = SrcSet(set = this.found, type = type)

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