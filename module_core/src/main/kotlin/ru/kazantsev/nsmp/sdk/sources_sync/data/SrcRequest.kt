package ru.kazantsev.nsmp.sdk.sources_sync.data

import kotlinx.serialization.Serializable

@Serializable
class SrcRequest(
    val modules: Set<String> = setOf(),
    val allModules: Boolean = false,
    val modulesExcluded: Set<String> = setOf(),
    val scripts: Set<String> = setOf(),
    val allScripts: Boolean = false,
    val scriptCodesExcluded: Set<String> = setOf(),
    val advImports: Set<String> = setOf(),
    val allAdvImports: Boolean = false,
    val advImportsExcluded: Set<String> = setOf(),
)
