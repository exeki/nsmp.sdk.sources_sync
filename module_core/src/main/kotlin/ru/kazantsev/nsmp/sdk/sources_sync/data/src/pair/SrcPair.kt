package ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCode

open class SrcPair<T : ISrcCode, E : ISrcCode>(
    override val code: String,
    override val local: T?,
    override val remote: E?,
) : ISrcPair<T, E>