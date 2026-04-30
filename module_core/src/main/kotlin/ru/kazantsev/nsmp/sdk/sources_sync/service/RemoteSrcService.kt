package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcTextChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.lookup.NotFoundRemoteSrcFileLookupRootException
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.ConversionService
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.SrcArchiveService
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.SrcSyncConnector

class RemoteSrcService(connectorParams: ConnectorParams) {

    private val srcArchiveService = SrcArchiveService()
    private val connector = SrcSyncConnector(connectorParams)
    private val conversionService = ConversionService()

    private val log = LoggerFactory.getLogger(javaClass)

    private fun <T : ISrc> buildSrcLookupResult(
        request: SrcSetRequest,
        remote: SrcSet<T>
    ): SrcLookup<T> {
        if (request.type != remote.type) throw IllegalArgumentException("Cannot compare remote because request and src set has different types")
        val result = if (request.all) SrcLookup(found = remote, type = request.type)
        else SrcLookup(
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
    fun lookupRemoteSrc(req: SrcRequest): SrcLookupRoot<SrcTextChecksum> {
        log.info("Remote src request started: {}", req)
        val srcArchive = connector.getSrc(req)
        val remoteSrc = srcArchiveService.unpackSrcArchive(srcArchive)
        val lookupResult = SrcLookupRoot.byEnumIterator {
            buildSrcLookupResult(req.getSetRequest(it), remoteSrc[it])
        }
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
    fun lookupRemoteSrcInfo(req: SrcRequest): SrcLookupRoot<SrcChecksum> {
        log.info("Remote src info request started: {}", req)
        val remoteRoot = conversionService.convertRemoteInfoRoot(connector.getSrcInfo(req))
        val lookupResultRoot = SrcLookupRoot.byEnumIterator {
            buildSrcLookupResult(req.getSetRequest(it), remoteRoot[it])
        }
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
     * @throws NotFoundRemoteSrcFileLookupRootException если не все исходники найдены
     */
    @Throws(NotFoundRemoteSrcFileLookupRootException::class)
    fun getRemoteSrc(req: SrcRequest): SrcSetRoot<SrcTextChecksum> {
        val lookupResult = lookupRemoteSrc(req)
        NotFoundRemoteSrcFileLookupRootException.throwIfNecessary(lookupResult)
        return SrcSetRoot.fromLookupRoot(lookupResult)
    }

    /**
     * Получить информацию по исходникам с сервера
     * @param req запрос
     * @return src set root с информацией об исходниках с сервера
     */
    fun getRemoteSrcInfo(req: SrcRequest): SrcSetRoot<SrcChecksum> {
        return SrcSetRoot.fromLookupRoot(lookupRemoteSrcInfo(req))
    }

    /**
     * Отправить исходники на инсталляцию
     * @param fileSrcSetRoot root с исходниками к отправке
     * @return чексуммы загруженных исходников с сервера
     */
    fun <T : ISrcFile> sendRemoteSrc(fileSrcSetRoot: SrcSetRoot<T>): SrcSetRoot<SrcChecksum> {
        log.info(
            "Remote src upload started: scripts={}, modules={}, advImports={}",
            fileSrcSetRoot.scripts.size,
            fileSrcSetRoot.modules.size,
            fileSrcSetRoot.advImports.size
        )
        val archive = srcArchiveService.buildSrcArchive(fileSrcSetRoot)
        val checksums = connector.pushScripts(archive)
        log.info(
            "Remote src upload completed: scripts={}, modules={}, advImports={}",
            checksums.scripts.size,
            checksums.modules.size,
            checksums.advimports.size
        )
        return conversionService.convertChecksums(checksums)
    }
}
