package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SetRoot

class DuplicatedLocalSrcFileFoundException(lookupResult: SrcLookupResultRoot<*>) : LocalSrcException(getMessage(lookupResult)) {

    @Suppress("unused")
    val duplicatedSrcCodes = SetRoot(
        scripts = lookupResult.scripts.duplicated,
        modules = lookupResult.modules.duplicated,
        advImports = lookupResult.advImports.duplicated,
    )

    companion object  {
         fun throwIfNecessary(lookupResult : SrcLookupResultRoot<*>)  {
            if(lookupResult.hasDuplicates()) throw DuplicatedLocalSrcFileFoundException(lookupResult)
        }

        private fun getMessage(lookupResult: SrcLookupResultRoot<*>): String {
            return buildString {
                append("Some local src files duplicated:")
                if (lookupResult.scripts.duplicated.isNotEmpty()) append(
                    " scripts: ${lookupResult.scripts.notFound.joinToString(", ")}"
                )
                if (lookupResult.modules.duplicated.isNotEmpty()) append(
                    " modules: ${lookupResult.modules.notFound.joinToString(", ")}"
                )
                if (lookupResult.advImports.duplicated.isNotEmpty()) append(
                    " advImports: ${lookupResult.advImports.notFound.joinToString(", ")}"
                )
            }
        }
    }
}
