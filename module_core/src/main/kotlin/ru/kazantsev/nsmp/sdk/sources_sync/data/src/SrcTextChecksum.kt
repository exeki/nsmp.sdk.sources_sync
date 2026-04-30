package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcTextChecksum

class SrcTextChecksum(
    override val code: String,
    override val checksum: String?,
    override val text: String
) : ISrcTextChecksum