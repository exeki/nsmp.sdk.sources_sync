package ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote

class InfoFileNotFound : RemoteSrcException(MSG) {
    companion object {
        const val MSG = "File \"info.json\" not found in archive from installation"
    }
}