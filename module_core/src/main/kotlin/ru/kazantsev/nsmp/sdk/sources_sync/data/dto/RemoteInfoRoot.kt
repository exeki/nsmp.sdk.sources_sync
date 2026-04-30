package ru.kazantsev.nsmp.sdk.sources_sync.data.dto

import kotlinx.serialization.Serializable

@Serializable
class RemoteInfoRoot(
    val scripts: List<RemoteInfo>,
    val modules: List<RemoteInfo>,
    val advImports: List<RemoteInfo>,
)