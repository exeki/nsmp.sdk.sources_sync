package ru.kazantsev.nsmp.sdk.sources_sync

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptyPullResponse
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.SyncCheckFailedException
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcChecksumService
import ru.kazantsev.nsmp.sdk.sources_sync.service.RemoteSrcService
import ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.LocalSrcService

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcSyncService(
    connectorParams: ConnectorParams,
    srcFoldersParams: SrcFoldersParams
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val srcChecksumService = SrcChecksumService()
    val remoteSrcService = RemoteSrcService(connectorParams)
    val localSrcService = LocalSrcService(srcFoldersParams)

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun pull(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
        log.info("Fetch started: req={}", req)
        val root = remoteSrcService.getRemoteSrc(req)
        if (root.isEmpty()) throw EmptyPullResponse()
        //TODO проверки
        val result = localSrcService.whiteLocalSrc(root.convertToSrcSetRoot())
        log.info(
            "Fetch completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    /**
     * Получает чексуммы с сервера и сравнивает их с локальным хранилищем.
     */
    fun syncCheck(req: SrcRequest): SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>> {
        log.info("Diff started: {}", req)
        val localRoot = localSrcService.getLocalSrc(req)
        return syncCheck(localRoot)
    }

    private fun <T : ISrcCodeChecksum> syncCheck(localRoot: SrcSetRoot<T>): SrcSetRoot<SrcSyncCheckPair<T, RemoteInfo>> {
        val req = localRoot.convertToRequest()
        val remoteRoot = remoteSrcService.getRemoteSrcInfo(req).convertToSrcSetRoot()
        return srcChecksumService.compareSrcSetRoots(localRoot, remoteRoot)
    }

    /**
     * Собирает локальные исходники, проверяет их чексуммами и отправляет на сервер.
     */
    fun push(
        req: SrcRequest,
        force: Boolean = false
    ): SrcSetRoot<LocalFileInfo> {
        log.info("Push started: req={}, force={}", req, force)
        val localRoot = localSrcService.getLocalSrc(req)
        if (!force) {
            val remoteSrcInfo = remoteSrcService.getRemoteSrcInfo(req).convertToSrcSetRoot()
            val diff = srcChecksumService.compareSrcSetRoots(localRoot, remoteSrcInfo)
            SyncCheckFailedException.throwIfNecessary(diff)
        } else log.warn("force push enabled!")
        val scriptsChecksums = remoteSrcService.sendRemoteSrc(localRoot)
        localSrcService.updateInfoFile(scriptsChecksums)
        log.info(
            "Push completed: scripts={}, modules={}, advImports={}",
            scriptsChecksums.scripts.size,
            scriptsChecksums.modules.size,
            scriptsChecksums.advimports.size
        )
        return localSrcService.getLocalSrc(req)
    }


}
