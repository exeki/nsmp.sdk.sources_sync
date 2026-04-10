package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcDtoRoot(
    val scripts: List<SrcDto> = emptyList(),
    val modules: List<SrcDto> = emptyList(),
    val advImports: List<SrcDto> = emptyList()
)
