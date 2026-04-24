package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

interface ISrcSetRoot<T : ISrcCode> : IRoot<ISrcSet<T>> {
    override val scripts: ISrcSet<T>
    override val modules: ISrcSet<T>
    override val advImports: ISrcSet<T>

    fun convertToRequest(): SrcRequest

    fun <K : ISrcCode> convert(transform: (T) -> K): ISrcSetRoot<K>

    fun <K : ISrcCode> convert(
        scriptTransform: (T) -> K,
        moduleTransform: (T) -> K,
        advImportTransform: (T) -> K
    ): ISrcSetRoot<K>

    fun any(predicate: (T) -> Boolean): Boolean
}