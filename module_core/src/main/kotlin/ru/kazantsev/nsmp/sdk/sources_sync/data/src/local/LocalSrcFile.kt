package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IHasSrcCode
import java.io.File

class LocalSrcFile(
    override val code: String,
    val file: File
) : IHasSrcCode
