package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
class LocalStorageInfo(
    override val code: String,
    override val checksum: String?,
    @Suppress("unused")
    @Serializable(with = LocalDateTimeAsStringSerializer::class)
    val lastSync: LocalDateTime = now()
) : ISrcChecksum {

    constructor(info: ISrcChecksum) : this(info.code, info.checksum)

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