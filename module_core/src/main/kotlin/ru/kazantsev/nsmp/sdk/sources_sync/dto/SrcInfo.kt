package ru.kazantsev.nsmp.sdk.sources_sync.dto

/**
 * Информация о исходнике
 */
class SrcInfo(
    val checksum: String,
    val code: String
) {
    constructor() : this("", "")
}