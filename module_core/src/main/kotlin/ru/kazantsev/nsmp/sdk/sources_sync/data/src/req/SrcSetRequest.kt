package ru.kazantsev.nsmp.sdk.sources_sync.data.src.req

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

class SrcSetRequest(
    val type: SrcType,
    val includedCodes: Set<String> = setOf(),
    val all: Boolean = false,
    val excludedCodes: Set<String> = setOf()
)
