package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local

interface ILocalChecksum : ILocalSrc {
    val checksum : String?
}