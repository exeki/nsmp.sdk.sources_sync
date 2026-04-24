package ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteChecksum

class SrcSyncCheckPair<L : ILocalChecksum, R : IRemoteChecksum>(
    code: String,
    local: L,
    remote: R?
) : SrcPair<L, R>(
    code = code,
    local = local,
    remote = remote
) {
    val conflict: Boolean = remote?.checksum != null && local.checksum != remote.checksum
}