package ru.kazantsev.nsmp.sdk.sources_sync.dto

open class SrcRequest(
    val modules: List<String> = listOf(),
    val allModules: Boolean = false,
    val scripts: List<String> = listOf(),
    val allScripts: Boolean = false,
    val advImports: List<String> = listOf(),
    val allAdvImports: Boolean = false,
)
