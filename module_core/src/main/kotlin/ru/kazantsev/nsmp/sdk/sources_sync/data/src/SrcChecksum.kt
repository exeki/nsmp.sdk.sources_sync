package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum

/**
 * Информация об исходнике
 */
@Serializable
open class SrcChecksum(
    override val code: String,
    override val checksum: String?,
) : ISrcChecksum