package ru.kazantsev.nsmp.sdk.sources_sync.data.dto

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum

@Serializable
class RemoteInfo(
    override val code: String,
    override val checksum: String,
) : ISrcChecksum