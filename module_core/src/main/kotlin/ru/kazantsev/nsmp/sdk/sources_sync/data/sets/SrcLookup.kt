package ru.kazantsev.nsmp.sdk.sources_sync.data.sets

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc

class SrcLookup<T : ISrc>(
    val type: SrcType,
    val notFound: Set<String> = setOf(),
    val duplicated: Set<String> = setOf(),
    found: Set<T>
) {

    val found: SrcSet<T> = SrcSet(set = found, type = type)

    fun convertToSrcSet() = SrcSet(set = this.found, type = type)

    companion object {
        fun <T : ISrc> empty(type: SrcType): SrcLookup<T> {
            return SrcLookup(
                type = type,
                notFound = setOf(),
                duplicated = setOf(),
                found = emptySet()
            )
        }
    }
}