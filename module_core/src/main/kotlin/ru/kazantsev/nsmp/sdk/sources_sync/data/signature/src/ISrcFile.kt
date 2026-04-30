package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src

import java.io.File

interface ISrcFile : ISrc {
    val file: File
}