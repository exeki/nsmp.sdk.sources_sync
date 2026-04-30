package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.LocalSrcLookupException

class SyncCheckEmptySrcSetRootException : LocalSrcLookupException(MSG) {
    companion object : LookupResultExceptionCompanion() {

        fun throwIfNecessary(srcSetRoot: SrcSetRoot<*>) {
            if (srcSetRoot.all { it.value.isEmpty() }) throw SyncCheckEmptySrcSetRootException()
        }

        const val MSG = "Got empty local src set root  while executing command \"sync check\""
    }
}