package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.LocalStorageInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.Root
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcLookupRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcLookup
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum
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

    val srcInfoFileRoot = Root.byEnumIterator { type ->
        SrcInfoFile(
            projectPath = projectPath,
            fileName = "${type.code}.json",
            type = type,
            json = json
        )
    }

    fun lookupLocalInfoSrcSet(req: SrcSetRequest): SrcLookup<LocalStorageInfo> {
        return srcInfoFileRoot[req.type].lookupLocalInfoSrcSet(req)
    }

    /**
     * Выполнить поиск по информации по исходникам
     * @param req запрос
     * @return результаты поиска
     */
    fun lookupLocalInfoSrcSetRoot(req: SrcRequest): SrcLookupRoot<LocalStorageInfo> {
        val filtered = SrcLookupRoot.byEnumIterator { type ->
            srcInfoFileRoot[type].lookupLocalInfoSrcSet(req.getSetRequest(type))
        }
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
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun <T : ISrcChecksum> updateInfoFile(root: SrcSetRoot<T>): SrcSetRoot<T> {
        log.debug(
            "Update local info file started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        root.forEach { (type, _) ->
            srcInfoFileRoot[type].update(root[type])
        }
        log.debug("Update local info files completed")
        return root
    }


}
