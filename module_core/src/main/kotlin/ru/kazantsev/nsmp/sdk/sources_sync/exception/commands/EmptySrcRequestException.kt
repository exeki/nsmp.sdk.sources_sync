package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

class EmptySrcRequestException(
    @Suppress("unused")
    val srcRequest: SrcRequest
) : CommandException("Empty src request") {
    companion object {
        fun throwIfNecessary(req: SrcRequest) {
            if (req.isEmpty()) throw EmptySrcRequestException(req)
        }

        const val MSG = "Empty src request"
    }
}