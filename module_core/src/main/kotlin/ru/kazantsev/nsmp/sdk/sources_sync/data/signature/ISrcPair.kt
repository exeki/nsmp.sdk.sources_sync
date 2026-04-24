package ru.kazantsev.nsmp.sdk.sources_sync.data.signature

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode

interface ISrcPair<T : ISrcCode, E : ISrcCode> : ISrcCode {
    override val code: String
    val local: T?
    val remote: E?
}
