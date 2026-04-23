package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import kotlinx.serialization.Serializable

@Serializable
class RemoteSrcInfo(
    val code: String,
    val checksum: String,
)