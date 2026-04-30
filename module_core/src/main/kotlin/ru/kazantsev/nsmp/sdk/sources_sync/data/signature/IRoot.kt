package ru.kazantsev.nsmp.sdk.sources_sync.data.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

interface IRoot<T : Any> : Map<SrcType, T> {
    val scripts: T
        get() = get(SrcType.SCRIPT)
    val modules: T
        get() = get(SrcType.MODULE)
    val advImports: T
        get() = get(SrcType.ADV_IMPORT)
    val map : Map<SrcType, T>

    override val size: Int
        get() = map.size

    override val keys: Set<SrcType>
        get() = map.keys

    override val values: Collection<T>
        get() = map.values

    override val entries: Set<Map.Entry<SrcType, T>>
        get() = map.entries

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun containsKey(key: SrcType): Boolean = map.containsKey(key)

    override fun containsValue(value: T): Boolean = map.containsValue(value)

    override fun get(key : SrcType) : T {
        return map.getValue(key)
    }
}