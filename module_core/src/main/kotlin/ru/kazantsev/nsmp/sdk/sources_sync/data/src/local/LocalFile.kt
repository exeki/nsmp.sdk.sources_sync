package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import java.io.File

class LocalFile(
    override val code: String,
    override val file: File
) : ISrcCode, ILocalFile
