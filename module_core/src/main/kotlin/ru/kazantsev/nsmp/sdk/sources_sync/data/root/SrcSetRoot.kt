package ru.kazantsev.nsmp.sdk.sources_sync.data.root

import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

open class SrcSetRoot<T : ISrc> protected constructor(
    map: Map<SrcType, SrcSet<T>>
) : Root<SrcSet<T>>(map) {

    /*
    constructor(
        scripts: Set<T>,
        modules: Set<T>,
        advImports: Set<T>
    ) : this(
        mapOf(
            SrcType.SCRIPT to SrcSet(scripts, SrcType.SCRIPT),
            SrcType.MODULE to SrcSet(modules, SrcType.MODULE),
            SrcType.ADV_IMPORT to SrcSet(advImports, SrcType.ADV_IMPORT),
        )
    )
     */

    fun convertToRequest(): SrcRequest = SrcRequest(
        scripts = this.scripts.map { it.code }.toSet(),
        modules = this.modules.map { it.code }.toSet(),
        advImports = this.advImports.map { it.code }.toSet()
    )

    fun <K : ISrc> convert(transform: (SrcType, T) -> K): SrcSetRoot<K> {
        val resultMap = map.map { (type, ts) ->
            type to SrcSet(ts.map { transform(type, it) }.toSet(), type)
        }.toMap()
        return SrcSetRoot(resultMap)
    }

    companion object {
        fun <T : ISrc> byEnumIterator(collector: (SrcType) -> Set<T>): SrcSetRoot<T> {
            return SrcSetRoot(SrcType.entries.associateWith {
                val set = collector(it)
                set as? SrcSet ?: SrcSet(set, it)
            })
        }

        fun <T : ISrc> empty(): SrcSetRoot<T> {
            return byEnumIterator { SrcSet.empty(it) }
        }

        fun <T : ISrc> fromLookupRoot(lookupRoot: SrcLookupRoot<T>): SrcSetRoot<T> {
            return SrcSetRoot(lookupRoot.map { entry -> entry.key to entry.value.found }.toMap())
        }
    }
}