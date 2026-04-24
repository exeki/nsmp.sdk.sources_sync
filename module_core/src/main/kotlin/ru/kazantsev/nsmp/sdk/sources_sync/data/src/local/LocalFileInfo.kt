package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import java.io.File

class LocalFileInfo(
    override val code: String,
    override val file: File,
    val info: LocalInfo?
) : ILocalFile, ILocalChecksum {
    override val checksum: String?
        get() = info?.checksum
}
