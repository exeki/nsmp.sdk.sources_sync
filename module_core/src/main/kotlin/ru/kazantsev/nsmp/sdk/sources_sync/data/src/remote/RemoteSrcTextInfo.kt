package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.IRemoteSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum

/**
 * Данные о полученном исходнике
 */
class RemoteSrcTextInfo(
    /**
     * Информация о полученном исходнике
     */
    val info: RemoteInfo,
    /**
     * Текст исходника
     */
    val text: String
) : ISrcCodeChecksum, IRemoteSrc {

    override val code: String
        get() = info.code

    override val checksum: String
        get() = info.checksum

}