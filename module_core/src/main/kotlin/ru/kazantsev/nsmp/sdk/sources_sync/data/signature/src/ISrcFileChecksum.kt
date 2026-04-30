package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src

import java.io.File

interface ISrcFileChecksum : ISrcFile, ISrcChecksum {
    override val file: File
    override val checksum: String?
}