package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote

interface IRemoteChecksum : IRemoteSrc {
    val checksum : String?
}