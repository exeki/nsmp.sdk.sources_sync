package ru.kazantsev.nsmp.sdk.sources_sync.data.signature

interface ISrcRoot<T : Any> {
    val scripts: T
    val modules: T
    val advImports: T

    fun isEmpty(): Boolean = !isNotEmpty()
    fun isNotEmpty(): Boolean

}