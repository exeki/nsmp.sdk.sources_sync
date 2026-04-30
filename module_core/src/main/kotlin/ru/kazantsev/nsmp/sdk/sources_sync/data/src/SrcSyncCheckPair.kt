package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum

class SrcSyncCheckPair<L : ISrcChecksum, R : ISrcChecksum>(
    code: String,
    left: L,
    right: R?
) : SrcPair<L, R>(
    code = code,
    left = left,
    right = right
) {
    val conflict: Boolean = right?.checksum != null && left.checksum != right.checksum
}