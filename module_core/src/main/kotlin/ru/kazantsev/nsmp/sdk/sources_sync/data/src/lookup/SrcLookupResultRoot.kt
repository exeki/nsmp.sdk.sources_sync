package ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root.IRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

class SrcLookupResultRoot<T : ISrcCode>(
    override val scripts: SrcLookupResult<T>,
    override val modules: SrcLookupResult<T>,
    override val advImports: SrcLookupResult<T>
) : IRoot<SrcLookupResult<T>> {

    override fun isNotEmpty(): Boolean {
        return scripts.found.isNotEmpty() || modules.found.isNotEmpty() || advImports.found.isNotEmpty()
    }

    fun hasNotFound(): Boolean {
        return scripts.notFound.isNotEmpty() || modules.notFound.isNotEmpty() || advImports.notFound.isNotEmpty()
    }

    fun hasDuplicates(): Boolean {
        return scripts.duplicated.isNotEmpty() || modules.duplicated.isNotEmpty() || advImports.duplicated.isNotEmpty()
    }

    fun convertToSrcSetRoot(): SrcSetRoot<T> = SrcSetRoot(
        scripts = this.scripts.found,
        modules = this.modules.found,
        advImports = this.advImports.found
    )

    private fun <K : ISrcCode> convertSrcLookupResult(
        srcLookupResult: SrcLookupResult<T>,
        convertor: (T) -> K
    ): SrcLookupResult<K> = SrcLookupResult(
        found = srcLookupResult.found.map(convertor).toSet(),
        notFound = srcLookupResult.notFound,
        duplicated = srcLookupResult.duplicated,
        type = srcLookupResult.type,
    )

    fun <K : ISrcCode> convert(convertor: (T) -> K): SrcLookupResultRoot<K> = SrcLookupResultRoot(
        scripts = convertSrcLookupResult(this.scripts, convertor),
        modules = convertSrcLookupResult(this.modules, convertor),
        advImports = convertSrcLookupResult(this.advImports, convertor)
    )

    companion object {
        @Suppress("unused")
        fun <T : ISrcCode> empty(): SrcLookupResultRoot<T> {
            return SrcLookupResultRoot(
                scripts = SrcLookupResult.empty(SrcType.SCRIPT),
                modules = SrcLookupResult.empty(SrcType.MODULE),
                advImports = SrcLookupResult.empty(SrcType.ADV_IMPORT)
            )
        }
    }
}