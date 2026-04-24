package ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum

class SrcSyncCheckPair<T : ISrcCodeChecksum, E : ISrcCodeChecksum>(
    code: String,
    local: T,
    remote: E?
) : SrcPair<T, E>(
    code = code,
    local = local,
    remote = remote
) {
    @Suppress("unused")
    val conflict : Boolean = remote?.checksum != null && local.checksum != remote.checksum
}