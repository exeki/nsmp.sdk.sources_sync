package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteChecksum

@Serializable
class RemoteInfo(
    override val code: String,
    override val checksum: String,
) : IRemoteChecksum