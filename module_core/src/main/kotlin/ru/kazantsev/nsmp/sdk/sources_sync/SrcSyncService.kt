package ru.kazantsev.nsmp.sdk.sources_sync

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.EmptySrcRequestException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.SyncCheckFailedException
import ru.kazantsev.nsmp.sdk.sources_sync.service.ComparisonService
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

    val comparisonService = ComparisonService()
    val remoteSrcService = RemoteSrcService(connectorParams)
    val localSrcService = LocalSrcService(srcFoldersParams)

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets
     * @param req запрос, в соответствии с которым будет сформирован запрос к серверу
     * @return
     */
    fun pull(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Fetch started: req={}", req)
        val root = remoteSrcService.getRemoteSrc(req)
        val result = localSrcService.whiteLocalSrc(root)
        log.info(
            "Fetch completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    /**
     * Получает чексуммы с сервера и сравнивает их с локальным хранилищем
     * @param req запрос, в соответствии с которым будет проведен поиск исходников для сверки
     * @return набор сетов, который состоят из пар локальной и удаленной информации об исходнике
     */
    fun syncCheck(req: SrcRequest): SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Diff started: {}", req)
        val localRoot = localSrcService.getLocalSrc(req)
        val result = syncCheck(localRoot)
        log.info(
            "Diff completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    private fun <T : ILocalChecksum> syncCheck(localRoot: SrcSetRoot<T>): SrcSetRoot<SrcSyncCheckPair<T, RemoteInfo>> {
        val req = localRoot.convertToRequest()
        log.debug(
            "Sync check comparison started: scripts={}, modules={}, advImports={}",
            localRoot.scripts.size,
            localRoot.modules.size,
            localRoot.advImports.size
        )
        val remoteRoot = remoteSrcService.getRemoteSrcInfo(req)
        val diff = comparisonService.compareSyncCheck(localRoot, remoteRoot)
        log.debug(
            "Sync check comparison completed: scripts={}, modules={}, advImports={}",
            diff.scripts.size,
            diff.modules.size,
            diff.advImports.size
        )
        return diff
    }

    /**
     * Собирает локальные исходники, проверяет их чексуммами и отправляет на сервер.
     * @param req запрос, в соответствии с которым будут собраны исходники для отправки
     * @param force при указании флага пропускает сверку по чексуммам
     * @return информация об исходниках после изменения и информация о чексуммах сервера до изменения
     */
    fun push(
        req: SrcRequest,
        force: Boolean = false
    ): SrcSetRoot<SrcPair<LocalFileInfo, RemoteInfo>> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Push started: req={}, force={}", req, force)
        val localRoot = localSrcService.getLocalSrc(req)
        val remoteRoot = remoteSrcService.getRemoteSrcInfo(req)
        if (!force) {
            val diff = comparisonService.compareSyncCheck(localRoot, remoteRoot)
            SyncCheckFailedException.throwIfNecessary(diff)
        } else log.warn("force push enabled!")
        val scriptChecksums = remoteSrcService.sendRemoteSrc(localRoot)
        val updatedLocalInfo = localSrcService.updateInfoFile(scriptChecksums)
        log.info(
            "Push completed: scripts={}, modules={}, advImports={}",
            scriptChecksums.scripts.size,
            scriptChecksums.modules.size,
            scriptChecksums.advimports.size
        )
        return comparisonService.compareSrcSetRoots(
            remoteRoot = remoteRoot,
            localRoot = localSrcService.compareLocalFileAndInfo(
                fileRoot = localRoot,
                infoRoot = updatedLocalInfo
            )
        )
    }
}
