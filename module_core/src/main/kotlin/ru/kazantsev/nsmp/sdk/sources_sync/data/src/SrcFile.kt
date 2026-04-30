package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFile
import java.io.File

class SrcFile(
    override val code: String,
    override val file: File
) : ISrcFile
