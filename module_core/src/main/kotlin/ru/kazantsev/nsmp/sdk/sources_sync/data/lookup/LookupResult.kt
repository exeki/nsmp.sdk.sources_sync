package ru.kazantsev.nsmp.sdk.sources_sync.data.lookup

class LookupResult<T> (
    val notFound: Set<String>,
    val duplicated : Set<String>,
    val found: Set<T>
) {
    companion object {
        fun <T> empty() : LookupResult<T> {
            return LookupResult(
                notFound = setOf(),
                duplicated = setOf(),
                found = emptySet()
            )
        }
    }
}