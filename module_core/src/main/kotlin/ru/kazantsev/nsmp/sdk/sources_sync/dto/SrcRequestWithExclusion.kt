package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcRequestWithExclusion(
    modules: List<String> = listOf(),
    allModules: Boolean = false,
    val modulesExcluded: List<String> = listOf(),
    scripts: List<String> = listOf(),
    allScripts: Boolean = false,
    val scriptsExcluded: List<String> = listOf(),
    advImports: List<String> = listOf(),
    allAdvImports: Boolean = false,
    val advImportsExcluded: List<String> = listOf(),
) : SrcRequest(
    modules = modules,
    allModules = allModules,
    scripts = scripts,
    allScripts = allScripts,
    advImports = advImports,
    allAdvImports = allAdvImports
)