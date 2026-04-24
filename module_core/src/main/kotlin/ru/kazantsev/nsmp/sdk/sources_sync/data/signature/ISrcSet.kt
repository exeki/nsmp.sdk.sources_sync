package ru.kazantsev.nsmp.sdk.sources_sync.data.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

interface ISrcSet<T : ISrcCode> : Set<T> {
    val type: SrcType

    fun getByCode(code: String): T?

    fun containsCode(code: String): Boolean

    fun getSrcMap(): Map<String, T>

    fun <K : ISrcCode> convert(transform: (T) -> K): ISrcSet<K>

}