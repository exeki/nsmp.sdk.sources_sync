package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteChecksum

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
) : IRemoteChecksum {

    override val code: String
        get() = info.code

    override val checksum: String
        get() = info.checksum

}