package ru.kazantsev.nsmp.sdk.sources_sync.dto

class SrcInfoRoot(
    val modules: List<SrcInfo>,
    val scripts: List<SrcInfo>
) {
    constructor() : this(listOf(), listOf())
}
