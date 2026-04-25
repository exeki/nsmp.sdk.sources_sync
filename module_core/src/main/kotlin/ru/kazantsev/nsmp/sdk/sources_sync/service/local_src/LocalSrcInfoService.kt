package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.storage.SrcInfoFile
import java.nio.file.Path

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class LocalSrcInfoService(private val projectPath: Path) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val scriptsSrcInfoFile = SrcInfoFile(
        projectPath = projectPath,
        fileName = Constants.SCRIPTS_INFO_FILE_NAME,
        type = SrcType.SCRIPT,
        json = json
    )

    val modulesSrcInfoFile = SrcInfoFile(
        projectPath = projectPath,
        fileName = Constants.MODULES_INFO_FILE_NAME,
        type = SrcType.MODULE,
        json = json
    )

    val advImportsSrcInfoFile = SrcInfoFile(
        projectPath = projectPath,
        fileName = Constants.ADV_IMPORTS_INFO_FILE_NAME,
        type = SrcType.ADV_IMPORT,
        json = json
    )






    /**
     * Выполнить поиск по информации по исходникам
     * @param req запрос
     * @return результаты поиска
     */
    fun lookupLocalSrcInfo(req: SrcRequest): SrcLookupResultRoot<LocalInfo> {
        val filtered = SrcLookupResultRoot(
            scripts = scriptsSrcInfoFile.lookupLocalInfo(req.getScriptsRequest()),
            modules = modulesSrcInfoFile.lookupLocalInfo(req.getModulesRequest()),
            advImports = advImportsSrcInfoFile.lookupLocalInfo(req.getAdvImportsRequest()),
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
        scriptsSrcInfoFile.update(root.scripts)
        modulesSrcInfoFile.update(root.modules)
        advImportsSrcInfoFile.update(root.advImports)
        log.debug("Update local info files completed")
        return root
    }


}
