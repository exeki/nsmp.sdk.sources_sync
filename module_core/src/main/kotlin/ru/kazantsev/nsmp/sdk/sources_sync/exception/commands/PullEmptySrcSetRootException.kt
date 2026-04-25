package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.LookupResultExceptionCompanion
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.LocalSrcLookupException

class PullEmptySrcSetRootException : LocalSrcLookupException(MSG) {
    companion object : LookupResultExceptionCompanion() {

        fun throwIfNecessary(srcSetRoot: SrcSetRoot<*>) {
            if (srcSetRoot.isEmpty()) throw PullEmptySrcSetRootException()
        }

        const val MSG = "Got empty remote src set root while executing command \"pull\""
    }
}