package ru.kazantsev.nsmp.sdk.sources_sync.data.src

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.root.ISetRoot

open class SetRoot<T : Any>(
    override val scripts: Set<T>,
    override val modules: Set<T>,
    override val advImports: Set<T>
) : ISetRoot<T> {

    override fun isNotEmpty(): Boolean {
        return scripts.isNotEmpty() || modules.isNotEmpty() || advImports.isNotEmpty()
    }

    override fun <K : Any> convert(transform: (T) -> K): ISetRoot<K> = SetRoot(
        scripts = this.scripts.map(transform).toSet(),
        modules = this.modules.map(transform).toSet(),
        advImports = this.advImports.map(transform).toSet()
    )

    companion object {
        fun <T : ISrcCode> empty(): SetRoot<T> {
            return SetRoot(
                scripts = emptySet(),
                modules = emptySet(),
                advImports = emptySet(),
            )
        }
    }
}