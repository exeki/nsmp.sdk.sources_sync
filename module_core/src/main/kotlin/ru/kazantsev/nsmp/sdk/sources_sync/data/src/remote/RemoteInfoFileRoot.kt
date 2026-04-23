package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

class RemoteInfoFileRoot(
    val scripts: List<RemoteSrcInfo>,
    val modules: List<RemoteSrcInfo>,
    val advImports: List<RemoteSrcInfo>,
)