package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple

import java.io.File

interface ILocalFile : ILocalSrc {
    val file: File
}