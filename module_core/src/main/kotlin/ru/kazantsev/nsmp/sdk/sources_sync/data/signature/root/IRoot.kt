package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root

interface IRoot<T : Any> {
    val scripts: T
    val modules: T
    val advImports: T

    fun isEmpty(): Boolean = !isNotEmpty()
    fun isNotEmpty(): Boolean

    @Suppress("unused")
    val entries : Set<T>
        get() = setOf(scripts, modules, advImports)
}