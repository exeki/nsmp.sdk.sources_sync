package ru.kazantsev.nsmp.sdk.sources_sync.data.sets

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc

open class SrcSet<T : ISrc>(
    val map: Map<String, T>,
    override val type: SrcType
) : ISrcSet<T> {

    constructor(set: Set<T>, type: SrcType) : this(set.associateBy { it.code }, type)

    override val size: Int = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: T): Boolean = map.containsKey(element.code)

    override fun iterator(): Iterator<T> = map.values.iterator()

    override fun containsAll(elements: Collection<T>): Boolean = !elements.any { !map.containsKey(it.code) }

    override fun getByCode(code: String): T? = map[code]

    override fun containsCode(code: String): Boolean = map.containsKey(code)

    override fun getSrcMap(): Map<String, T> = this.map

    override fun <K : ISrc> convert(transform: (T) -> K): SrcSet<K> {
        return SrcSet(this.map(transform).toSet(), this.type)
    }

    override fun convertToRequest(): SrcSetRequest {
        return SrcSetRequest(
            type = this.type,
            includedCodes = this.map.keys.toSet()
        )
    }

    companion object {
        fun <T : ISrc> empty(type: SrcType): SrcSet<T> = SrcSet(setOf(), type)
    }
}