package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcInfo

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class SrcChecksumService {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Возвращает только те элементы, checksum которых отличается или отсутствует локально.
     */
    fun compareSrcInfo(remoteLocalSrcInfo: SrcSetRoot<RemoteSrcInfo>, localSrcInfo: SrcSetRoot<LocalSrcInfo>): SrcSetRoot<LocalSrcInfo> {
        log.debug(
            "Checksum compare started: remoteScripts={}, remoteModules={}, remoteAdvImports={}. localScripts={}, localModules={}, localAdvImports={}",
            remoteLocalSrcInfo.scripts.size,
            remoteLocalSrcInfo.modules.size,
            remoteLocalSrcInfo.advImports.size,
            localSrcInfo.scripts.size,
            localSrcInfo.modules.size,
            localSrcInfo.advImports.size
        )
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }
        val localAdvImportsByCode = localSrcInfo.advImports.associateBy { it.code }
        val result = SrcSetRoot(
            scripts = remoteLocalSrcInfo.scripts.filter { remoteInfo ->
                val localInfo = localScriptsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }.toSet(),
            modules = remoteLocalSrcInfo.modules.filter { remoteInfo ->
                val localInfo = localModulesByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }.toSet(),
            advImports = remoteLocalSrcInfo.advImports.filter { remoteInfo ->
                val localInfo = localAdvImportsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }.toSet(),
        )
        log.debug(
            "Checksum compare completed: changedScripts={}, changedModules={}, changedAdvImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }
}
