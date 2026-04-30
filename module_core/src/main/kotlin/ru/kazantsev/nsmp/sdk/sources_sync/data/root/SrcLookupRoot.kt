package ru.kazantsev.nsmp.sdk.sources_sync.data.root

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc

open class SrcLookupRoot<T : ISrc> protected constructor(
    map: Map<SrcType, SrcLookup<T>>
) : Root<SrcLookup<T>>(map) {

    /*
    constructor(
        scripts: SrcLookup<T>,
        modules: SrcLookup<T>,
        advImports: SrcLookup<T>
    ) : this(
        mapOf(
            SrcType.SCRIPT to scripts,
            SrcType.MODULE to modules,
            SrcType.ADV_IMPORT to advImports,
        )
    )
     */

    fun hasNotFound(): Boolean {
        return scripts.notFound.isNotEmpty() || modules.notFound.isNotEmpty() || advImports.notFound.isNotEmpty()
    }

    fun hasDuplicates(): Boolean {
        return scripts.duplicated.isNotEmpty() || modules.duplicated.isNotEmpty() || advImports.duplicated.isNotEmpty()
    }

    companion object {
        fun <T : ISrc> byEnumIterator(collector: (SrcType) -> SrcLookup<T>): SrcLookupRoot<T> {
            return SrcLookupRoot(SrcType.entries.associateWith { collector(it) })
        }

        fun <T : ISrc> empty(): SrcLookupRoot<T> {
            return byEnumIterator { SrcLookup.empty(it) }
        }
    }
}