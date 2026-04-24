package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.signature.LookupThrowableIfNecessary

class LocalSrcFilesNotFoundException(
    lookupResult: SrcLookupResultRoot<*>
) : LocalSrcException(getMessage(lookupResult)) {

    val notFoundSrcCodes = SetRoot(
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound,
    )

    companion object : LookupThrowableIfNecessary {

        override fun throwIfNecessary(lookupResult : SrcLookupResultRoot<*>) {
            if(lookupResult.hasNotFound()) throw LocalSrcFilesNotFoundException(lookupResult)
        }

        private fun getMessage(lookupResult: SrcLookupResultRoot<*>): String {
            return buildString {
                append("Some local src files not found:")
                if (lookupResult.scripts.notFound.isNotEmpty()) append(
                    " scripts: ${lookupResult.scripts.notFound.joinToString(", ")}"
                )
                if (lookupResult.modules.notFound.isNotEmpty()) append(
                    " modules: ${lookupResult.modules.notFound.joinToString(", ")}"
                )
                if (lookupResult.advImports.notFound.isNotEmpty()) append(
                    " advImports: ${lookupResult.advImports.notFound.joinToString(", ")}"
                )
            }
        }
    }
}