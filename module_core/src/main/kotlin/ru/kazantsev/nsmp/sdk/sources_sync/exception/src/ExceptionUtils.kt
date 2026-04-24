package ru.kazantsev.nsmp.sdk.sources_sync.exception.src

class ExceptionUtils {
    companion object {
        fun buildMessageForLookup(
            start: String,
            scripts: Set<String>,
            modules: Set<String>,
            advImports: Set<String>
        ): String {
            return buildString {
                append(start)
                if (scripts.isNotEmpty()) append(" scripts: $scripts.joinToString(", ")}")
                if (modules.isNotEmpty()) append(" modules: ${modules.joinToString(", ")}")
                if (advImports.isNotEmpty()) append(" advImports: ${advImports.joinToString(", ")}")
            }
        }
    }
}