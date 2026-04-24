package ru.kazantsev.nsmp.sdk.sources_sync.data.src

enum class SrcFormat(val code: String, val hasPackage: Boolean) {
    GROOVY("groovy", true),
    XML("xml", false),;
}