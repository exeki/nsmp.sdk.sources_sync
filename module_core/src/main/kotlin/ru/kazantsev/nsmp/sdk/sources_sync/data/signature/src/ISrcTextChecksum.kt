package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src

interface ISrcTextChecksum : ISrcChecksum, ISrcText {
    override val text : String
    override val checksum : String?
}