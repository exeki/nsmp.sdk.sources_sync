package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class SrcChecksumService {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Возвращает только те элементы, checksum которых отличается или отсутствует локально.
     */
    fun compareSrcInfo(remoteSrcInfo: SrcInfoRoot, localSrcInfo: SrcInfoRoot): SrcInfoRoot {
        log.debug(
            "Checksum compare started: remoteScripts={}, remoteModules={}, remoteAdvImports={}. localScripts={}, localModules={}, localAdvImports={}",
            remoteSrcInfo.scripts.size,
            remoteSrcInfo.modules.size,
            remoteSrcInfo.advImports.size,
            localSrcInfo.scripts.size,
            localSrcInfo.modules.size,
            localSrcInfo.advImports.size
        )
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }
        val localAdvImportsByCode = localSrcInfo.advImports.associateBy { it.code }
        val result = SrcInfoRoot(
            scripts = remoteSrcInfo.scripts.filter { remoteInfo ->
                val localInfo = localScriptsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            },
            modules = remoteSrcInfo.modules.filter { remoteInfo ->
                val localInfo = localModulesByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            },
            advImports = remoteSrcInfo.advImports.filter { remoteInfo ->
                val localInfo = localAdvImportsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            },
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
