package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcFileDtoRoot(
    val scripts: List<SrcFileDto> = emptyList(),
    val modules: List<SrcFileDto> = emptyList(),
    val advImports: List<SrcFileDto> = emptyList()
)
