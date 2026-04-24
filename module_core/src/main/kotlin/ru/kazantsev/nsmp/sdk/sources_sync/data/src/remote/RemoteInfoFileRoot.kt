package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import kotlinx.serialization.Serializable

@Serializable
class RemoteInfoFileRoot(
    val scripts: Set<RemoteInfo>,
    val modules: Set<RemoteInfo>,
    val advImports: Set<RemoteInfo>,
)