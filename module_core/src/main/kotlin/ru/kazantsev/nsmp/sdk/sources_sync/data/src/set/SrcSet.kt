package ru.kazantsev.nsmp.sdk.sources_sync.data.src.set

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

open class SrcSet<T : ISrcCode>(
    set: Set<T>,
    override val type: SrcType
) : ISrcSet<T> {

    protected val map: Map<String, T> = set.associateBy { it.code }

    override val size: Int = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: T): Boolean = map.containsKey(element.code)

    override fun iterator(): Iterator<T> = map.values.iterator()

    override fun containsAll(elements: Collection<T>): Boolean = !elements.any { !map.containsKey(it.code) }

    override fun getByCode(code: String): T? = map[code]

    override fun containsCode(code: String): Boolean = map.containsKey(code)

    override fun getSrcMap(): Map<String, T> = this.map

    override fun <K : ISrcCode> convert(transform: (T) -> K): SrcSet<K> {
        return SrcSet(this.map(transform).toSet(), this.type)
    }
}