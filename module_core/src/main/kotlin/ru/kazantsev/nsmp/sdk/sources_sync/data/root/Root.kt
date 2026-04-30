package ru.kazantsev.nsmp.sdk.sources_sync.data.root

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IRoot

open class Root<T : Any> protected constructor(
    override val map: Map<SrcType, T>
) : IRoot<T> {

    init {
        if (SrcType.entries.size != map.size) throw RuntimeException("Invalid root map size")
    }

    /*
    constructor(
        scripts: T,
        modules: T,
        advImports: T,
    ) : this(
        mapOf(
            SrcType.SCRIPT to scripts,
            SrcType.MODULE to modules,
            SrcType.ADV_IMPORT to advImports
        )
    )
     */

    companion object {
        fun <T : Any> byEnumIterator(collector: (SrcType) -> T): Root<T> {
            return Root(SrcType.entries.associateWith { collector(it) })
        }
    }

}