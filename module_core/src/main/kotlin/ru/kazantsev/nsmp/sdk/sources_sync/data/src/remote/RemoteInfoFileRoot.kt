package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import kotlinx.serialization.Serializable

@Serializable
class RemoteInfoFileRoot(
    val scripts: List<RemoteInfo>,
    val modules: List<RemoteInfo>,
    val advImports: List<RemoteInfo>,
)