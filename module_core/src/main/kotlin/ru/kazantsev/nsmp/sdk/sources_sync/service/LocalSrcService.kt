package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupResultException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupResultRootException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupResultException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupResultRootException
import ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.LocalSrcFileService
import ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.LocalSrcInfoService
import ru.kazantsev.nsmp.sdk.sources_sync.service.utils.ComparisonService

class LocalSrcService(
    srcFoldersParams: SrcFoldersParams,
    private val comparisonService: ComparisonService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val localSrcFileService = LocalSrcFileService(srcFoldersParams)
    private val localSrcInfoService = LocalSrcInfoService(srcFoldersParams.getProjectAbsolutePath())

    /**
     * Получить src set по запросу
     * @param req запрос
     * @return src set наполненный в соответствии с запросом
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupResultException если искомое не найден
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupResultException если файл дублирован в папке исходниками
     */
    //Внешний API
    @Suppress("unused")
    @Throws(DuplicatedLocalSrcFileLookupResultException::class, NotFoundLocalSrcFileLookupResultException::class)
    fun getLocalSrcSet(req: SrcSetRequest): SrcSet<LocalFileInfo> {
        log.info("Read local src set started: {}", req)
        val filesLookupResult = localSrcFileService.lookupLocalFileSrcSet(req)
        NotFoundLocalSrcFileLookupResultException.throwIfNecessary(filesLookupResult)
        DuplicatedLocalSrcFileLookupResultException.throwIfNecessary(filesLookupResult)
        val filesSrcSet = filesLookupResult.convertToSrcSet()
        val infoReq = filesSrcSet.convertToRequest()
        val result = comparisonService.uniteLocalFileInfoSrcSet(
            fileSrcSet = filesSrcSet,
            infoSrcSet = localSrcInfoService.lookupLocalInfoSrcSet(infoReq).convertToSrcSet()
        )
        log.info("Read local src completed: size={}", result.size)
        return result
    }

    /**
     * Получить src set root по запросу
     * @param req запрос
     * @return src set root наполненный в соответствии с запросом
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupResultRootException если искомое не найден
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupResultRootException если файл дублирован в папке исходниками
     */
    @Throws(DuplicatedLocalSrcFileLookupResultRootException::class, NotFoundLocalSrcFileLookupResultRootException::class)
    fun getLocalSrcSetRoot(req: SrcRequest): SrcSetRoot<LocalFileInfo> {
        log.info("Read local src set root started: {}", req)
        val filesLookupResultRoot = localSrcFileService.lookupLocalFileSrcSetRoot(req)
        NotFoundLocalSrcFileLookupResultRootException.throwIfNecessary(filesLookupResultRoot)
        DuplicatedLocalSrcFileLookupResultRootException.throwIfNecessary(filesLookupResultRoot)
        val filesRoot = filesLookupResultRoot.convertToSrcSetRoot()
        val infoReq = filesRoot.convertToRequest()
        val result = comparisonService.uniteLocalFileInfoSrcSetRoot(
            fileRoot = filesRoot,
            infoRoot = localSrcInfoService.lookupLocalInfoSrcSetRoot(infoReq).convertToSrcSetRoot()
        )
        log.info(
            "Read local src set root completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    /**
     * Обновить локальный src set root информацией
     * @param scriptChecksums чексуммы с сервера для обновления
     * @return перечень обновленной информации
     */
    fun updateLocalInfoSrcSetRoot(scriptChecksums: ScriptChecksums): SrcSetRoot<LocalInfo> {
        val root = SrcSetRoot(
            scripts = scriptChecksums.scripts.map { LocalInfo(it) }.toSet(),
            modules = scriptChecksums.modules.map { LocalInfo(it) }.toSet(),
            advImports = scriptChecksums.advimports.map { LocalInfo(it) }.toSet()
        )
        return updateLocalInfoSrcSetRoot(root)
    }

    /**
     * Обновить локальный src set root информацией
     * @param srcSetRoot src set root с информацией для обновления
     * @return перечень обновленной информации
     */
    fun updateLocalInfoSrcSetRoot(srcSetRoot: SrcSetRoot<LocalInfo>): SrcSetRoot<LocalInfo> {
        log.debug(
            "Update local info requested: scripts={}, modules={}, advImports={}",
            srcSetRoot.scripts.size,
            srcSetRoot.modules.size,
            srcSetRoot.advImports.size
        )
        return localSrcInfoService.updateInfoFile(srcSetRoot)
    }

    /**
     * Обновить локальный src set root информацией
     * @param remoteTextInfoSrcSetRoot src set root с информацией для обновления
     * @return файлы после записи
     */
    fun updateLocalFileSrcSetRoot(remoteTextInfoSrcSetRoot: SrcSetRoot<RemoteTextInfo>): SrcSetRoot<LocalFileInfo> {
        log.info(
            "Write local src started: scripts={}, modules={}, advImports={}",
            remoteTextInfoSrcSetRoot.scripts.size,
            remoteTextInfoSrcSetRoot.modules.size,
            remoteTextInfoSrcSetRoot.advImports.size
        )
        updateLocalInfoSrcSetRoot(remoteTextInfoSrcSetRoot.convert { LocalInfo(it.info) })
        val result = localSrcFileService.whiteLocalFiles(remoteTextInfoSrcSetRoot)
        log.info("Write local src completed")
        return result
    }
}