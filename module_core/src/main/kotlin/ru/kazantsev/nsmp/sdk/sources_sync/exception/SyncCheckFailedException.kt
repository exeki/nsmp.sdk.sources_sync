package ru.kazantsev.nsmp.sdk.sources_sync.exception

import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot

class SyncCheckFailedException(message: String, val srcInfoRoot: SrcInfoRoot) : RuntimeException(message)