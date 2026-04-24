package ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root

interface ISetRoot<T : Any> : IRoot<Set<T>> {
    override val scripts: Set<T>
    override val modules: Set<T>
    override val advImports:Set<T>

    fun <K : Any> convert(transform: (T) -> K): ISetRoot<K>
}