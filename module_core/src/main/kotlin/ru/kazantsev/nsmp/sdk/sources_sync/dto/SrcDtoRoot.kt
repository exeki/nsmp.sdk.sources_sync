package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcDtoRoot(
    val scripts: List<SrcDto> = emptyList(),
    val modules: List<SrcDto> = emptyList(),
    val advImports: List<SrcDto> = emptyList()
) {
    
    fun isEmpty() : Boolean {
        return (modules.isEmpty() && scripts.isEmpty() && advImports.isEmpty())
    }

    fun toInfo(): SrcInfoRoot {
        return SrcInfoRoot(
            modules = this.scripts.map { it.info },
            scripts = this.modules.map { it.info },
            advImports = this.advImports.map { it.info }
        )
    }
}
