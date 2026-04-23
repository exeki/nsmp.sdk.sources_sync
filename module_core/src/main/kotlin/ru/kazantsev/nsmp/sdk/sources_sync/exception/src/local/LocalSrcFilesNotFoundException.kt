package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.signature.LookupThrowableIfNecessary

class LocalSrcFilesNotFoundException(
    lookupResult: LookupResultRoot<*>
) : LocalSrcException(getMessage(lookupResult)) {

    val notFoundSrcCodes = SrcSetRoot(
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound,
    )

    companion object : LookupThrowableIfNecessary {

        override fun throwIfNecessary(lookupResult : LookupResultRoot<*>) {
            if(lookupResult.hasNotFound()) throw LocalSrcFilesNotFoundException(lookupResult)
        }

        private fun getMessage(lookupResult: LookupResultRoot<*>): String {
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