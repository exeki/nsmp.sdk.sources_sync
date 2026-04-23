package ru.kazantsev.nsmp.sdk.sources_sync.data.sync_check

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.IHasSrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo
import java.io.File

class SyncCheckResult(
    override val code: String,
    val file: File,
    val localInfo: LocalSrcInfo,
    val remoteChecksum: String
) : IHasSrcCode