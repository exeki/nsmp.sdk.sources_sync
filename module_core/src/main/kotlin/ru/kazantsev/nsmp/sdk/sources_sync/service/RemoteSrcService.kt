package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.ISrcCode
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.NoRemoteSrcFilesException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.RemoteSrcFilesNotFoundException

class RemoteSrcService(connectorParams: ConnectorParams) {

    val srcArchiveService = SrcArchiveService()
    val connector = SrcSyncConnector(connectorParams)

    private val log = LoggerFactory.getLogger(javaClass)

    private fun <T : ISrcCode> compareRemoteAndRequested(
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
            "Comparison of remote and requested result: found={}, notFound={}, duplicated={}",
            result.found.size,
            result.notFound.size,
            result.duplicated.size
        )
        return result
    }

    /**
     * Получить исходники с инсталляции
     */
    fun lookupRemoteSrc(req: SrcRequest): SrcLookupResultRoot<RemoteSrcTextInfo> {
        log.info("Remote src request started: {}", req)
        val srcArchive = connector.getSrc(req)
        val remoteSrc = srcArchiveService.unpackSrcArchive(srcArchive)
        val lookupResult = SrcLookupResultRoot(
            scripts = compareRemoteAndRequested(req.getScriptsRequest(), remoteSrc.scripts),
            modules = compareRemoteAndRequested(req.getModulesRequest(), remoteSrc.modules),
            advImports = compareRemoteAndRequested(req.getAdvImportsRequest(), remoteSrc.advImports)
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
     * Получить исходники с инсталляции, но с проверочками
     */
    fun getRemoteSrc(req: SrcRequest): SrcSetRoot<RemoteSrcTextInfo> {
        val lookupResult = lookupRemoteSrc(req)
        NoRemoteSrcFilesException.throwIfNecessary(lookupResult)
        RemoteSrcFilesNotFoundException.throwIfNecessary(lookupResult)
        return lookupResult.convertToSrcSetRoot()
    }

    /**
     * Получает с сервера актуальную информацию о чексуммах исходников.
     */
    fun lookupRemoteSrcInfo(req: SrcRequest): SrcLookupResultRoot<RemoteInfo> {
        log.info("Remote src info request started: {}", req)
        val remoteData = connector.getSrcInfo(req)
        val remoteRoot = SrcSetRoot(
            scripts = remoteData.scripts,
            modules = remoteData.modules,
            advImports = remoteData.advImports
        )
        val lookupResult = SrcLookupResultRoot(
            scripts = compareRemoteAndRequested(req.getScriptsRequest(), remoteRoot.scripts),
            modules = compareRemoteAndRequested(req.getModulesRequest(), remoteRoot.modules),
            advImports = compareRemoteAndRequested(req.getAdvImportsRequest(), remoteRoot.advImports),
        )
        log.info(
            "Remote src info completed: scripts={}, modules={}, advImports={}",
            lookupResult.scripts.found.size,
            lookupResult.modules.found.size,
            lookupResult.advImports.found.size
        )
        return lookupResult
    }

    fun getRemoteSrcInfo(req: SrcRequest): SrcSetRoot<RemoteInfo> {
        return lookupRemoteSrcInfo(req).convertToSrcSetRoot()
    }

    /**
     * Отправить исходники на инсталляцию
     */
    fun <T : ILocalFile> sendRemoteSrc(root: SrcSetRoot<T>): ScriptChecksums {
        log.info(
            "Remote src upload started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        val archive = srcArchiveService.buildSrcArchive(root)
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
