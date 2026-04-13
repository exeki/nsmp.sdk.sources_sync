package ru.kazantsev.nsmp.sdk.sources_sync.dto

import kotlinx.serialization.Serializable

@Serializable
class SrcRequest(
    val modules: List<String> = listOf(),
    val allModules: Boolean = false,
    val modulesExcluded: List<String> = listOf(),
    val scripts: List<String> = listOf(),
    val allScripts: Boolean = false,
    val scriptsExcluded: List<String> = listOf(),
    val advImports: List<String> = listOf(),
    val allAdvImports: Boolean = false,
    val advImportsExcluded: List<String> = listOf(),
)
