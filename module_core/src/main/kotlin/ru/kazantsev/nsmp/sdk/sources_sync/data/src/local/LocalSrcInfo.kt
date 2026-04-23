package ru.kazantsev.nsmp.sdk.sources_sync.data.src.local

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IHasSrcCode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Информация о исходнике
 */
@Serializable
class LocalSrcInfo(
    override val code: String,
    val checksum: String,
    val lastSync: String = nowFormatted()
) : IHasSrcCode {
    constructor() : this("", "")

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        fun nowFormatted(): String {
            return LocalDateTime.now().format(formatter)
        }
    }
}
