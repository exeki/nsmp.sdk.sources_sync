package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ILocalSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Информация о исходнике
 */
@Serializable
open class LocalInfo(
    override val code: String,
    override val checksum: String,
    val lastSync: String = nowFormatted()
) : ISrcCodeChecksum, ILocalSrc {
    constructor(remoteInfo: RemoteInfo) : this(remoteInfo.code, remoteInfo.checksum)
    constructor(remoteInfo: SrcChecksum) : this(remoteInfo.code, remoteInfo.checksum)

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        fun nowFormatted(): String {
            return LocalDateTime.now().format(formatter)
        }
    }
}
