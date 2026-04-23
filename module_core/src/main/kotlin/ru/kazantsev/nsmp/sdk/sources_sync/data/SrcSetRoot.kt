package ru.kazantsev.nsmp.sdk.sources_sync.data

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcRoot

@Serializable
open class SrcSetRoot<T : Any>(
    override val scripts: Set<T>,
    override val modules: Set<T>,
    override val advImports: Set<T>
) : ISrcRoot<Set<T>> {

    override fun isNotEmpty(): Boolean {
        return scripts.isNotEmpty() || modules.isNotEmpty() || advImports.isNotEmpty()
    }

    fun convertToRequest(converter: (T) -> String): SrcRequest = SrcRequest(
        scripts = this.scripts.map(converter).toSet(),
        modules = this.modules.map(converter).toSet(),
        advImports = this.advImports.map(converter).toSet()
    )

    fun <K : Any> convert(convertor: (T) -> K): SrcSetRoot<K> {
        return SrcSetRoot(
            scripts = this.scripts.map(convertor).toSet(),
            modules = this.scripts.map(convertor).toSet(),
            advImports = this.scripts.map(convertor).toSet()
        )
    }

    fun <K : Any> convert(
        scriptConvertor: (T) -> K,
        moduleConvertor: (T) -> K,
        advImportConvertor: (T) -> K
    ): SrcSetRoot<K> {
        return SrcSetRoot(
            scripts = this.scripts.map(scriptConvertor).toSet(),
            modules = this.scripts.map(moduleConvertor).toSet(),
            advImports = this.scripts.map(advImportConvertor).toSet()
        )
    }

    companion object {
        fun <T : Any> empty(): SrcSetRoot<T> {
            return SrcSetRoot(
                scripts = emptySet(),
                modules = emptySet(),
                advImports = emptySet(),
            )
        }
    }
}