package ru.kazantsev.nsmp.sdk.sources_sync.dto

/**
 * Данные о полученном исходнике
 */
class SrcDto(
    /**
     * Информация о полученном исходнике
     */
    val info: SrcInfo,
    /**
     * Текст исходника
     */
    val text: String
)