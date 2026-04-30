package ru.kazantsev.nsmp.sdk.sources_sync.data.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest

interface ISrcSet<T : ISrc> : Set<T> {
    val type: SrcType

    fun getByCode(code: String): T?

    fun containsCode(code: String): Boolean

    fun getSrcMap(): Map<String, T>

    fun <K : ISrc> convert(transform: (T) -> K): ISrcSet<K>

    fun convertToRequest() : SrcSetRequest
}