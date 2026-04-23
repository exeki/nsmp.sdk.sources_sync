package ru.kazantsev.nsmp.sdk.sources_sync.data.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IHasSrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcRoot
import kotlin.collections.map

class LookupResultRoot<T : Any>(
    override val scripts: LookupResult<T>,
    override val modules: LookupResult<T>,
    override val advImports: LookupResult<T>
) : ISrcRoot<LookupResult<T>> {

    override fun isNotEmpty(): Boolean {
        return scripts.found.isNotEmpty() || modules.found.isNotEmpty() || advImports.found.isNotEmpty()
    }

    fun convertToRequest(converter: (T) -> String): SrcRequest = SrcRequest(
        scripts = this.scripts.found.map(converter).toSet(),
        modules = this.modules.found.map(converter).toSet(),
        advImports = this.advImports.found.map(converter).toSet()
    )

    fun hasNotFound(): Boolean {
        return scripts.notFound.isNotEmpty() || modules.notFound.isNotEmpty() || advImports.notFound.isNotEmpty()
    }

    fun hasDuplicates(): Boolean {
        return scripts.duplicated.isNotEmpty() || modules.duplicated.isNotEmpty() || advImports.duplicated.isNotEmpty()
    }

    fun <K : IHasSrcCode> convert(convertor: (T) -> K): SrcSetRoot<K> = SrcSetRoot(
        scripts = this.scripts.found.map(convertor).toSet(),
        modules = this.modules.found.map(convertor).toSet(),
        advImports = this.advImports.found.map(convertor).toSet()
    )

    fun <K : IHasSrcCode> convert(
        scriptConvertor: (T) -> K,
        moduleConvertor: (T) -> K,
        advImportConvertor: (T) -> K
    ): SrcSetRoot<K> = SrcSetRoot(
        scripts = this.scripts.found.map(scriptConvertor).toSet(),
        modules = this.modules.found.map(moduleConvertor).toSet(),
        advImports = this.advImports.found.map(advImportConvertor).toSet()
    )

    companion object {
        fun <T : IHasSrcCode> empty(): LookupResultRoot<T> {
            return LookupResultRoot(
                scripts = LookupResult.empty(),
                modules = LookupResult.empty(),
                advImports = LookupResult.empty()
            )
        }
    }
}