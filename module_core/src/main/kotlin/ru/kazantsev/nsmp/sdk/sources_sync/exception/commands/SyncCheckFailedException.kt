package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo

class SyncCheckFailedException(message: String, val localSrcInfoRoot: SrcSetRoot<LocalSrcInfo>) : RuntimeException(message)