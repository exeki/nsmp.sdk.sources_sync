package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot

interface ISrcSetRoot<T : ISrcCode> : IRoot<SrcSet<T>> {
    override val scripts: SrcSet<T>
    override val modules: SrcSet<T>
    override val advImports: SrcSet<T>

    fun convertToRequest(): SrcRequest

    fun <K : ISrcCode> convert(transform: (T) -> K): SrcSetRoot<K>

    fun <K : ISrcCode> convert(
        scriptTransform: (T) -> K,
        moduleTransform: (T) -> K,
        advImportTransform: (T) -> K
    ): SrcSetRoot<K>

    fun any(predicate : (T) -> Boolean): Boolean
}