package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair

class PushSyncCheckFailedException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    val localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>
) : CommandException(getMessage(localSrcInfoRoot)) {
    companion object {

        fun throwIfNecessary(localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>) {
            if (localSrcInfoRoot.any { entry -> entry.value.any { it.conflict } })
                throw PushSyncCheckFailedException(localSrcInfoRoot)
        }

        private fun getMessage(localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>): String {
            val conflictScripts = localSrcInfoRoot.scripts.filter { it.conflict }
            val conflictModules = localSrcInfoRoot.modules.filter { it.conflict }
            val conflictAdvImports = localSrcInfoRoot.advImports.filter { it.conflict }
            return buildString {
                append("Found conflicts is src checksums while checking sync:")
                if (conflictScripts.isNotEmpty())
                    append(" scripts: ${conflictScripts.joinToString(", ") { it.code }}")
                if (conflictModules.isNotEmpty())
                    append(" modules: ${conflictModules.joinToString(", ") { it.code }}")
                if (conflictAdvImports.isNotEmpty())
                    append(" advImports: ${conflictAdvImports.joinToString(", ") { it.code }}")
            }
        }
    }
}