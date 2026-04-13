package ru.kazantsev.nsmp.sdk.sources_sync.dto

import kotlinx.serialization.Serializable

/**
 * Информация о исходнике
 */
@Serializable
class SrcInfo(
    val checksum: String,
    val code: String
) {
    constructor() : this("", "")
}
