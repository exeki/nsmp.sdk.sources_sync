package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SetRoot

class RemoteSrcFilesNotFoundException(
    lookupResult: SrcLookupResultRoot<*>
) : RemoteSrcException(getMessage(lookupResult)) {

    val notFoundSrcCodes = SetRoot(
        scripts = lookupResult.scripts.notFound,
        modules = lookupResult.modules.notFound,
        advImports = lookupResult.advImports.notFound,
    )

    companion object  {

         fun throwIfNecessary(lookupResult : SrcLookupResultRoot<*>) {
            if(lookupResult.hasNotFound()) throw RemoteSrcFilesNotFoundException(lookupResult)
        }

        private fun getMessage(lookupResult: SrcLookupResultRoot<*>): String {
            return buildString {
                append("Some remote src files not found:")
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