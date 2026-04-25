package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup.NotFoundRemoteSrcFileLookupResultRootException
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.SrcArchiveService
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.SrcSyncConnector

class RemoteSrcService(connectorParams: ConnectorParams) {

    private val srcArchiveService = SrcArchiveService()
    private val connector = SrcSyncConnector(connectorParams)

    private val log = LoggerFactory.getLogger(javaClass)

    private fun <T : ISrcCode> buildSrcLookupResult(
        request: SrcSetRequest,
        remote: SrcSet<T>
    ): SrcLookupResult<T> {
        if (request.type != remote.type) throw IllegalArgumentException("Cannot compare remote because request and src set has different types")
        val result = if (request.all) SrcLookupResult(found = remote, type = request.type)
        else SrcLookupResult(
            type = request.type,
            found = remote,
            notFound = request.includedCodes
                .filter { !request.excludedCodes.contains(it) }
                .filter { !remote.containsCode(it) }
                .toSet()
        )
        log.debug(
            "buildSrcLookupResult: type={} found={}, notFound={}, duplicated={}",
            result.type.code,
            result.found.size,
            result.notFound.size,
            result.duplicated.size
        )
        return result
    }

    /**
     * Получить исходники с инсталляции
     * @param req запрос
     * @return результат поиска исходников
     */
    fun lookupRemoteSrc(req: SrcRequest): SrcLookupResultRoot<RemoteTextInfo> {
        log.info("Remote src request started: {}", req)
        val srcArchive = connector.getSrc(req)
        val remoteSrc = srcArchiveService.unpackSrcArchive(srcArchive)
        val lookupResult = SrcLookupResultRoot(
            scripts = buildSrcLookupResult(req.getScriptsRequest(), remoteSrc.scripts),
            modules = buildSrcLookupResult(req.getModulesRequest(), remoteSrc.modules),
            advImports = buildSrcLookupResult(req.getAdvImportsRequest(), remoteSrc.advImports)
        )
        log.info(
            "Fetch src completed: scripts={}, modules={}, advImports={}",
            lookupResult.scripts.found.size,
            lookupResult.modules.found.size,
            lookupResult.advImports.found.size
        )
        return lookupResult
    }

    /**
     * Получает с сервера актуальную информацию о чексуммах исходников
     * @param req запрос
     * @return src set root с информацией о исходниках
     */
    fun lookupRemoteSrcInfo(req: SrcRequest): SrcLookupResultRoot<RemoteInfo> {
        log.info("Remote src info request started: {}", req)
        val remoteData = connector.getSrcInfo(req)
        val remoteRoot = SrcSetRoot(
            scripts = remoteData.scripts,
            modules = remoteData.modules,
            advImports = remoteData.advImports
        )
        val lookupResultRoot = SrcLookupResultRoot(
            scripts = buildSrcLookupResult(req.getScriptsRequest(), remoteRoot.scripts),
            modules = buildSrcLookupResult(req.getModulesRequest(), remoteRoot.modules),
            advImports = buildSrcLookupResult(req.getAdvImportsRequest(), remoteRoot.advImports),
        )
        log.info(
            "Remote src info completed: scripts={}, modules={}, advImports={}",
            lookupResultRoot.scripts.found.size,
            lookupResultRoot.modules.found.size,
            lookupResultRoot.advImports.found.size
        )
        return lookupResultRoot
    }

    /**
     * Получить исходники с инсталляции, но с проверочками
     * @param req запрос
     * @return src set root с исходниками
     * @throws NotFoundRemoteSrcFileLookupResultRootException если не все исходники найдены
     */
    @Throws(NotFoundRemoteSrcFileLookupResultRootException::class)
    fun getRemoteSrc(req: SrcRequest): SrcSetRoot<RemoteTextInfo> {
        val lookupResult = lookupRemoteSrc(req)
        NotFoundRemoteSrcFileLookupResultRootException.throwIfNecessary(lookupResult)
        return lookupResult.convertToSrcSetRoot()
    }

    /**
     * Получить информацию по исходникам с сервера
     * @param req запрос
     * @return src set root с информацией об исходниках с сервера
     */
    fun getRemoteSrcInfo(req: SrcRequest): SrcSetRoot<RemoteInfo> {
        return lookupRemoteSrcInfo(req).convertToSrcSetRoot()
    }

    /**
     * Отправить исходники на инсталляцию
     * @param fileSrcSetRoot root с исходниками к отправке
     * @return чексуммы загруженных исходников с сервера
     */
    fun <T : ILocalFile> sendRemoteSrc(fileSrcSetRoot: SrcSetRoot<T>): ScriptChecksums {
        log.info(
            "Remote src upload started: scripts={}, modules={}, advImports={}",
            fileSrcSetRoot.scripts.size,
            fileSrcSetRoot.modules.size,
            fileSrcSetRoot.advImports.size
        )
        val archive = srcArchiveService.buildSrcArchive(fileSrcSetRoot)
        val result = connector.pushScripts(archive)
        log.info(
            "Remote src upload completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advimports.size
        )
        return result
    }
}
