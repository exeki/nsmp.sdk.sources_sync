package ru.kazantsev.nsmp.sdk.sources_sync.exception.commands

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo

class SyncCheckFailedException(
    @Suppress("CanBeParameter")
    val localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>>
) : CommandException(getMessage(localSrcInfoRoot)) {
    companion object {

        fun throwIfNecessary(localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>>) {
            if (localSrcInfoRoot.any { it.conflict }) throw SyncCheckFailedException(localSrcInfoRoot)
        }

        private fun getMessage(localSrcInfoRoot: SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>>): String {
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