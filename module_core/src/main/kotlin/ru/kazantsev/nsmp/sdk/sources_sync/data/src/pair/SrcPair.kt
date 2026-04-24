package ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteSrc

open class SrcPair<T : ILocalSrc, E : IRemoteSrc>(
    override val code: String,
    override val local: T,
    override val remote: E?,
) : ISrcPair<T, E>