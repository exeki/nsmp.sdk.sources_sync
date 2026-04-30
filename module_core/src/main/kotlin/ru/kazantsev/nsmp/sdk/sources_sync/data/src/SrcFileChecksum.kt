package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFileChecksum
import java.io.File

class SrcFileChecksum(
    override val code: String,
    override val file: File,
    override val checksum: String?
) : ISrcFileChecksum