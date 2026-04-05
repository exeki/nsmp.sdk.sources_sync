package ru.kazantsev.nsmp.sdk.sources_sync.dto

data class SrcCodesDto(
    val scripts: List<String> = emptyList(),
    val modules: List<String> = emptyList()
)