package ru.kazantsev.nsmp.sdk.sources_sync.data

enum class SrcFormat(val code: String, val hasPackage: Boolean) {
    GROOVY("groovy", true),
    XML("xml", false),;
}