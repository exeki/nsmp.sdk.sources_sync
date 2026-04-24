package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo

@Serializable
class LocalStorage (
    val scripts : Set<LocalInfo>,
    val modules : Set<LocalInfo>,
    val advImports : Set<LocalInfo>
) {
}