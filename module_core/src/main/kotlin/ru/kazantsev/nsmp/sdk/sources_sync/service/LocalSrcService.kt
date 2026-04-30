package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcTextChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupRootException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupRootException
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
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupException если искомое не найден
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupException если файл дублирован в папке исходниками
     */
    //Внешний API
    @Suppress("unused")
    @Throws(DuplicatedLocalSrcFileLookupException::class, NotFoundLocalSrcFileLookupException::class)
    fun getLocalSrcSet(req: SrcSetRequest): SrcSet<SrcFileChecksum> {
        log.info("Read local src set started: {}", req)
        val filesLookupResult = localSrcFileService.lookupLocalFileSrcSet(req)
        NotFoundLocalSrcFileLookupException.throwIfNecessary(filesLookupResult)
        DuplicatedLocalSrcFileLookupException.throwIfNecessary(filesLookupResult)
        val filesSrcSet = filesLookupResult.convertToSrcSet()
        val infoReq = filesSrcSet.convertToRequest()
        val result = comparisonService.uniteFileChecksumSrcSets(
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
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.NotFoundLocalSrcFileLookupRootException если искомое не найден
     * @throws ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.lookup.DuplicatedLocalSrcFileLookupRootException если файл дублирован в папке исходниками
     */
    @Throws(
        DuplicatedLocalSrcFileLookupRootException::class,
        NotFoundLocalSrcFileLookupRootException::class
    )
    fun getLocalSrcSetRoot(req: SrcRequest): SrcSetRoot<SrcFileChecksum> {
        log.info("Read local src set root started: {}", req)
        val filesLookupResultRoot = localSrcFileService.lookupLocalFileSrcSetRoot(req)
        NotFoundLocalSrcFileLookupRootException.throwIfNecessary(filesLookupResultRoot)
        DuplicatedLocalSrcFileLookupRootException.throwIfNecessary(filesLookupResultRoot)
        val filesRoot = SrcSetRoot.fromLookupRoot(filesLookupResultRoot)
        val infoReq = filesRoot.convertToRequest()
        val result = comparisonService.uniteFileChecksumSrcSetRoots(
            fileRoot = filesRoot,
            infoRoot = SrcSetRoot.fromLookupRoot(localSrcInfoService.lookupLocalInfoSrcSetRoot(infoReq))
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
     * @param srcSetRoot src set root с информацией для обновления
     * @return перечень обновленной информации
     */
    fun <T : ISrcChecksum> updateLocalInfoSrcSetRoot(srcSetRoot: SrcSetRoot<T>): SrcSetRoot<T> {
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
     * @param textSrcSetRoot src set root с информацией для обновления
     * @return файлы после записи
     */
    fun updateLocalFileSrcSetRoot(textSrcSetRoot: SrcSetRoot<SrcTextChecksum>): SrcSetRoot<SrcFileChecksum> {
        log.info(
            "Write local src started: scripts={}, modules={}, advImports={}",
            textSrcSetRoot.scripts.size,
            textSrcSetRoot.modules.size,
            textSrcSetRoot.advImports.size
        )
        updateLocalInfoSrcSetRoot(textSrcSetRoot)
        val files = localSrcFileService.whiteLocalFiles(textSrcSetRoot)
        log.info("Write local src completed")
        return comparisonService.uniteFileChecksumSrcSetRoots(files, textSrcSetRoot)
    }
}