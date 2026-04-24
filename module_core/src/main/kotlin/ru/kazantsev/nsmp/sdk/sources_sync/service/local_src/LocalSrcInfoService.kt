package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.LocalStorage
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import java.io.File
import java.nio.file.Path

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class LocalSrcInfoService(private val projectPath: Path) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    companion object {
        private const val SDK_DIR_PATH = ".nsmp_sdk"
        private const val INFO_FILE_NAME = "src_info.json"
    }

    private val infoFilePath: File
        get() = projectPath.resolve(SDK_DIR_PATH).resolve(INFO_FILE_NAME).toFile()

    /**
     * Возвращает файл локального хранилища метаданных.
     */
    @Suppress("unused")
    fun getInfoFile(): File = infoFilePath

    /**
     * Читает локальный файл с информацией целиком
     */
    fun readLocalSrcInfo(): LocalStorage {
        log.debug("Read local info: file={}", infoFilePath)
        if (!infoFilePath.exists() || infoFilePath.readText().isBlank()) {
            log.debug("Local info file not found or empty")
            return LocalStorage.empty()
        }
        val serializer = LocalStorage.serializer()
        return json.decodeFromString(serializer, infoFilePath.readText())
    }

    /**
     * Выполнить поиск по информации по исходникам
     * @param req запрос
     * @return результаты поиска
     */
    fun lookupLocalSrcInfo(req: SrcRequest): SrcLookupResultRoot<LocalInfo> {
        val localStorage = readLocalSrcInfo()
        val srcInfo = SrcSetRoot(
            scripts = localStorage.scripts,
            modules = localStorage.modules,
            advImports = localStorage.advImports
        )
        val filtered = SrcLookupResultRoot(
            scripts = lookupForLocalInfo(srcInfo.scripts, req.getScriptsRequest()),
            modules = lookupForLocalInfo(srcInfo.modules, req.getModulesRequest()),
            advImports = lookupForLocalInfo(srcInfo.advImports, req.getAdvImportsRequest()),
        )
        log.debug(
            "Scripts local info found: {}, notFound: {}, duplicated: {}",
            filtered.scripts.found.size,
            filtered.scripts.notFound.size,
            filtered.scripts.duplicated.size
        )
        log.debug(
            "Modules local info found: {}, notFound: {}, duplicated: {}",
            filtered.modules.found.size,
            filtered.modules.notFound.size,
            filtered.modules.duplicated.size
        )
        log.debug(
            "AdvImports local info found: {}, notFound: {}, duplicated: {}",
            filtered.advImports.found.size,
            filtered.advImports.notFound.size,
            filtered.advImports.duplicated.size
        )
        return filtered
    }

    /**
     * Получить информацию по исходникам
     * @param req запрос
     * @return набор сетов
     */
    fun getLocalSrcInfo(req: SrcRequest): SrcSetRoot<LocalInfo> {
        return lookupLocalSrcInfo(req).convertToSrcSetRoot()
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(root: SrcSetRoot<LocalInfo>): SrcSetRoot<LocalInfo> {
        log.debug(
            "Update local info file started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        val sdkDir = projectPath.resolve(SDK_DIR_PATH).toFile().apply { mkdirs() }
        val currentInfoFile = sdkDir.resolve(INFO_FILE_NAME)
        if (!currentInfoFile.exists()) {
            currentInfoFile.createNewFile()
        }

        val currentRoot = readLocalSrcInfo()
        val updatedRoot = LocalStorage(
            scripts = mergeEntries(currentRoot.scripts, root.scripts),
            modules = mergeEntries(currentRoot.modules, root.modules),
            advImports = mergeEntries(currentRoot.advImports, root.advImports),
        )

        currentInfoFile.writeText(json.encodeToString(updatedRoot))
        log.debug("Update local info file completed: file={}", currentInfoFile)
        return root
    }

    private fun mergeEntries(existingEntries: Set<LocalInfo>, incomingEntries: Set<LocalInfo>): Set<LocalInfo> {
        val byCode = linkedMapOf<String, LocalInfo>()

        existingEntries.forEach { info ->
            byCode[info.code] = info
        }

        incomingEntries.forEach { info ->
            byCode[info.code] = info
        }

        return byCode.values.toSet()
    }

    private fun lookupForLocalInfo(
        localSrcInfo: SrcSet<LocalInfo>,
        req: SrcSetRequest
    ): SrcLookupResult<LocalInfo> {
        return if (req.all) SrcLookupResult(
            found = localSrcInfo.filter { !req.excludedCodes.contains(it.code) }.toSet(),
            notFound = setOf(),
            duplicated = setOf(),
            type = req.type
        )
        else {
            val found: MutableSet<LocalInfo> = mutableSetOf()
            val notFound: MutableSet<String> = mutableSetOf()
            req.includedCodes.forEach { code ->
                if (!req.excludedCodes.contains(code)) {
                    val info = localSrcInfo.find { it.code == code }
                    if (info != null) found.add(info)
                    else notFound.add(code)
                }
            }
            SrcLookupResult(
                found = found,
                notFound = notFound,
                duplicated = setOf(),
                type = req.type
            )
        }
    }
}
