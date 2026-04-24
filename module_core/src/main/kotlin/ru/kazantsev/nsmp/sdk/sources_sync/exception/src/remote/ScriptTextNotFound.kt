package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

@Suppress("CanBeParameter")
class ScriptTextNotFound(val srcCode: String, val srcType: SrcType) :
    RemoteSrcException("$MSG: srcCode = $srcCode, srcType = $srcType") {
    companion object {
        const val MSG = "Src text not found"
    }
}