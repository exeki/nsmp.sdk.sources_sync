package ru.kazantsev.nsmp.sdk.sources_sync.data.src.set

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root.ISrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcRequest

open class SrcSetRoot<T : ISrcCode>(
    scripts: Set<T>,
    modules: Set<T>,
    advImports: Set<T>
) : ISrcSetRoot<T> {

    override val scripts: SrcSet<T> = SrcSet(scripts, SrcType.SCRIPT)
    override val modules: SrcSet<T> = SrcSet(modules, SrcType.MODULE)
    override val advImports: SrcSet<T> = SrcSet(advImports, SrcType.ADV_IMPORT)

    constructor(setRoot: SetRoot<T>) : this(setRoot.scripts, setRoot.modules, setRoot.advImports)

    override fun isNotEmpty(): Boolean {
        return scripts.isNotEmpty() || modules.isNotEmpty() || advImports.isNotEmpty()
    }

    override fun any(predicate: (T) -> Boolean): Boolean {
        return scripts.any(predicate) || modules.any(predicate) || advImports.any(predicate)
    }

    override fun convertToRequest(): SrcRequest = SrcRequest(
        scripts = this.scripts.map { it.code }.toSet(),
        modules = this.modules.map { it.code }.toSet(),
        advImports = this.advImports.map { it.code }.toSet()
    )

    override fun <K : ISrcCode> convert(transform: (T) -> K): SrcSetRoot<K> = convert(
        scriptTransform = transform,
        moduleTransform = transform,
        advImportTransform = transform
    )

    override fun <K : ISrcCode> convert(
        scriptTransform: (T) -> K,
        moduleTransform: (T) -> K,
        advImportTransform: (T) -> K
    ): SrcSetRoot<K> = SrcSetRoot(
        scripts = this.scripts.convert(scriptTransform),
        modules = this.scripts.convert(moduleTransform),
        advImports = this.scripts.convert(advImportTransform)
    )


    companion object {
        fun <T : ISrcCode> empty(): SrcSetRoot<T> = SrcSetRoot(
            scripts = emptySet(),
            modules = emptySet(),
            advImports = emptySet(),
        )
    }
}