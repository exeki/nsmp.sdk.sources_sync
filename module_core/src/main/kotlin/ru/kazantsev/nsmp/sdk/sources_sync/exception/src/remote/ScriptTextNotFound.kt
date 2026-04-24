package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

class ScriptTextNotFound(val srcCode : String, val srcType: SrcType) :
    RemoteSrcException("Src text not found: srcCode = $srcCode, srcType = $srcType")