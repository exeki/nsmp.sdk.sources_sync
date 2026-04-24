package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum
import java.io.File

class LocalFileInfo(
    override val code: String,
    override val file: File,
    val info: LocalInfo?
) : ILocalFile, ISrcCodeChecksum {
    override val checksum: String?
        get() = info?.checksum
}
