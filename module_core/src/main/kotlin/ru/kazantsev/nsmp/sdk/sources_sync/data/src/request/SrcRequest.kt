package ru.kazantsev.nsmp.sdk.sources_sync.data.src.request

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

@Serializable
class SrcRequest(
    val modules: Set<String> = setOf(),
    val allModules: Boolean = false,
    val modulesExcluded: Set<String> = setOf(),
    val scripts: Set<String> = setOf(),
    val allScripts: Boolean = false,
    val scriptsExcluded: Set<String> = setOf(),
    val advImports: Set<String> = setOf(),
    val allAdvImports: Boolean = false,
    val advImportsExcluded: Set<String> = setOf(),
) {

    fun getScriptsRequest(): SrcSetRequest {
        return SrcSetRequest(
            type = SrcType.SCRIPT,
            includedCodes = scripts,
            excludedCodes = scriptsExcluded,
            all = allScripts
        )
    }

    fun getModulesRequest(): SrcSetRequest {
        return SrcSetRequest(
            type = SrcType.MODULE,
            includedCodes = modules,
            excludedCodes = modulesExcluded,
            all = allModules
        )
    }

    fun getAdvImportsRequest(): SrcSetRequest {
        return SrcSetRequest(
            type = SrcType.ADV_IMPORT,
            includedCodes = advImports,
            excludedCodes = advImportsExcluded,
            all = allAdvImports
        )
    }

    fun isEmpty(): Boolean = getScriptsRequest().isEmpty()
            && getModulesRequest().isEmpty()
            && getAdvImportsRequest().isEmpty()

}
