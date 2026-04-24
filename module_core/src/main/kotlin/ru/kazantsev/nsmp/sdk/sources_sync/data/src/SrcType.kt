package ru.kazantsev.nsmp.sdk.sources_sync.data.src

enum class SrcType(val code: String, val format: SrcFormat) {
    MODULE("module", SrcFormat.GROOVY),
    ADV_IMPORT("advImport", SrcFormat.XML),
    SCRIPT("script", SrcFormat.GROOVY), ;
}