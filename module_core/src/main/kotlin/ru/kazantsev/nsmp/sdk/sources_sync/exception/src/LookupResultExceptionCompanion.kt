package ru.kazantsev.nsmp.sdk.sources_sync.exception.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

abstract class LookupResultExceptionCompanion {
    fun buildMessage(
        start: String,
        type : SrcType,
        codes : Set<String>,
    ): String {
        return buildString {
            append(start)
            append(" ${type.code}: ${codes.joinToString(", ")}")
        }
    }
}