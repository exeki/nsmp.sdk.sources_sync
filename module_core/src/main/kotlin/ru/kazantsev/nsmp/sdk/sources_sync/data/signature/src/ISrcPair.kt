package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src

interface ISrcPair<T : ISrc, E : ISrc> : ISrc {
    override val code: String
    val left: T?
    val right: E?
}