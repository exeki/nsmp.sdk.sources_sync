package ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote

/**
 * Данные о полученном исходнике
 */
class RemoteSrcTextInfo(
    /**
     * Информация о полученном исходнике
     */
    val info: RemoteSrcInfo,
    /**
     * Текст исходника
     */
    val text: String
)