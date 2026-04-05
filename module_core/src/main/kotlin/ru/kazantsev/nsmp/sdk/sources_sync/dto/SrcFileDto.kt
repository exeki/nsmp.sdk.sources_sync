package ru.kazantsev.nsmp.sdk.sources_sync.dto

import java.io.File

data class SrcFileDto(
    val code: String,
    val file: File
)
