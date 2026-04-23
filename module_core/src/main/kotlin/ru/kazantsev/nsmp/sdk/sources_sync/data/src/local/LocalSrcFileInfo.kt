package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IHasSrcCode
import java.io.File

class LocalSrcFileInfo(
    override val code: String,
    val info: LocalSrcInfo?,
    val file: File
) : IHasSrcCode
