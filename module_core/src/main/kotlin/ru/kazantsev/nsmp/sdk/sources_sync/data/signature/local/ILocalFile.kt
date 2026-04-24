package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local

import java.io.File

interface ILocalFile : ILocalSrc {
    val file: File
}