package ru.kazantsev.nsmp.sdk.sources_sync

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.*
import ru.kazantsev.nsmp.sdk.sources_sync.service.LocalSrcService
import ru.kazantsev.nsmp.sdk.sources_sync.service.RemoteSrcService
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.ComparisonService

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcSyncService(
    connectorParams: ConnectorParams,
    srcFoldersParams: SrcFoldersParams
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val comparisonService = ComparisonService()
    val remoteSrcService = RemoteSrcService(connectorParams)
    val localSrcService = LocalSrcService(srcFoldersParams, comparisonService)

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets
     * @param req запрос, в соответствии с которым будет сформирован запрос к серверу
     * @return src set root после изменения
     * @throws EmptySrcRequestException если передан пустой запрос
     * @throws PullEmptySrcSetRootException если по запросу не найдено ни одного исходника
     */
    @Throws(EmptySrcRequestException::class, PullEmptySrcSetRootException::class)
    fun pull(req: SrcRequest): SrcSetRoot<SrcFileChecksum> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Fetch started: req={}", req)
        val remoteSrcSetRoot = remoteSrcService.getRemoteSrc(req)
        PullEmptySrcSetRootException.throwIfNecessary(remoteSrcSetRoot)
        val result = localSrcService.updateLocalFileSrcSetRoot(remoteSrcSetRoot)
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
     * @throws EmptySrcRequestException если передан пустой запрос
     * @throws SyncCheckEmptySrcSetRootException если по запросу не найдено ни одного исходника
     */
    @Throws(EmptySrcRequestException::class, SyncCheckEmptySrcSetRootException::class)
    fun syncCheck(req: SrcRequest): SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Diff started: {}", req)
        val localSrcSetRoot = localSrcService.getLocalSrcSetRoot(req)
        SyncCheckEmptySrcSetRootException.throwIfNecessary(localSrcSetRoot)
        val result = syncCheck(localSrcSetRoot)
        log.info(
            "Diff completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    private fun <T : ISrcChecksum> syncCheck(localRoot: SrcSetRoot<T>): SrcSetRoot<SrcSyncCheckPair<T, SrcChecksum>> {
        val req = localRoot.convertToRequest()
        log.debug(
            "Sync check comparison started: scripts={}, modules={}, advImports={}",
            localRoot.scripts.size,
            localRoot.modules.size,
            localRoot.advImports.size
        )
        val remoteRoot = remoteSrcService.getRemoteSrcInfo(req)
        val diff = comparisonService.pairSyncCheck(localRoot, remoteRoot)
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
     * @throws EmptySrcRequestException если передан пустой запрос
     * @throws PushEmptySrcSetRootException если по запросу не найдено ни одного исходника
     */
    @Throws(EmptySrcRequestException::class, PushEmptySrcSetRootException::class)
    fun push(
        req: SrcRequest,
        force: Boolean = false
    ): SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>> {
        EmptySrcRequestException.throwIfNecessary(req)
        log.info("Push started: req={}, force={}", req, force)
        val localFileSrcSetRoot = localSrcService.getLocalSrcSetRoot(req)
        PushEmptySrcSetRootException.throwIfNecessary(localFileSrcSetRoot)
        val startRemoteInfoSrcSetRoot = remoteSrcService.getRemoteSrcInfo(req)
        if (!force) {
            val diff = comparisonService.pairSyncCheck(localFileSrcSetRoot, startRemoteInfoSrcSetRoot)
            PushSyncCheckFailedException.throwIfNecessary(diff)
        } else log.warn("force push enabled!")
        val newInfoSrcSetRoot = remoteSrcService.sendRemoteSrc(localFileSrcSetRoot)
        localSrcService.updateLocalInfoSrcSetRoot(newInfoSrcSetRoot)
        log.info(
            "Push completed: scripts={}, modules={}, advImports={}",
            newInfoSrcSetRoot.scripts.size,
            newInfoSrcSetRoot.modules.size,
            newInfoSrcSetRoot.advImports.size
        )
        return comparisonService.pairSrcSetRoots(
            rightSrcSetRoot = startRemoteInfoSrcSetRoot,
            leftSrcSetRoot = comparisonService.uniteFileChecksumSrcSetRoots(
                fileRoot = localFileSrcSetRoot,
                infoRoot = newInfoSrcSetRoot
            )
        )
    }
}
