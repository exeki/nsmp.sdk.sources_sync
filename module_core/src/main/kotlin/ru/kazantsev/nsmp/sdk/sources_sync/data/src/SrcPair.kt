package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcPair

open class SrcPair<T : ISrc, E : ISrc>(
    override val code: String,
    override val left: T,
    override val right: E?,
) : ISrcPair<T, E>