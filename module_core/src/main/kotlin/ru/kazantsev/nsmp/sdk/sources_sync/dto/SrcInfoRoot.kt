package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcInfoRoot(
    val modules: List<SrcInfo> = emptyList(),
    val scripts: List<SrcInfo> = emptyList(),
    val advImports: List<SrcInfo> = emptyList()
)
