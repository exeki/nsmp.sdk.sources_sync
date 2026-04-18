package ru.kazantsev.nsmp.sdk.sources_sync.dto

data class SrcCodeRoot(
    val scripts: List<String> = emptyList(),
    val modules: List<String> = emptyList(),
    val advImports: List<String> = emptyList()
) {
    fun isEmpty(): Boolean {
        return (modules.isEmpty() && scripts.isEmpty() && advImports.isEmpty())
    }
}