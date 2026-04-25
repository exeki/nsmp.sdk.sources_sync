package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Информация об исходнике
 */
@Serializable
open class LocalInfo(
    override val code: String,
    override val checksum: String,
    @Suppress("unused")
    @Serializable(with = LocalDateTimeAsStringSerializer::class)
    val lastSync: LocalDateTime = now()
) : ILocalChecksum {
    constructor(remoteInfo: RemoteInfo) : this(remoteInfo.code, remoteInfo.checksum)
    constructor(remoteInfo: SrcChecksum) : this(remoteInfo.code, remoteInfo.checksum)

    companion object {
        fun now(): LocalDateTime = LocalDateTime.now()
    }
}

object LocalDateTimeAsStringSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTimeAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val value = decoder.decodeString()
        return LocalDateTime.parse(value, formatter)
    }
}
