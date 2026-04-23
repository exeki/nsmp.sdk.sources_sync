package ru.kazantsev.nsmp.sdk.sources_sync.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResultRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcSetRoot
import java.io.File
import java.nio.file.Path

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class LocalSrcInfoService(
    private val projectPath: Path,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

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


    fun readLocalSrcInfo(): SrcSetRoot<LocalSrcInfo> {
        log.debug("Read local info: file={}", infoFilePath)
        if (!infoFilePath.exists() || infoFilePath.readText().isBlank()) {
            log.debug("Local info file not found or empty")
            return SrcSetRoot.empty()
        }
        val serializer = SrcSetRoot.serializer(LocalSrcInfo.serializer())
        return Json.decodeFromString(serializer, infoFilePath.readText())
    }

    /**
     * Читает локальную информацию об исходниках.
     *
     * При необходимости фильтрует данные по кодам scripts/modules.
     */
    fun readLocalSrcInfo(req: SrcRequest): LookupResultRoot<LocalSrcInfo> {
        val srcInfo = readLocalSrcInfo()
        val filtered = LookupResultRoot(
            scripts = lookupForInfo(srcInfo.scripts, req.scripts, req.allScripts, req.scriptCodesExcluded),
            modules = lookupForInfo(srcInfo.modules, req.modules, req.allModules, req.modulesExcluded),
            advImports = lookupForInfo(srcInfo.advImports, req.advImports, req.allAdvImports, req.advImportsExcluded),
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
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(root: SrcSetRoot<LocalSrcInfo>) {
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

        val rootObject = if (currentInfoFile.readText().isBlank()) objectMapper.createObjectNode()
        else objectMapper.readTree(currentInfoFile) as ObjectNode

        val updatedRoot = objectMapper.createObjectNode().apply {
            set<ArrayNode>("scripts", mergeEntries(rootObject.get("scripts"), root.scripts))
            set<ArrayNode>("modules", mergeEntries(rootObject.get("modules"), root.modules))
            set<ArrayNode>("advImports", mergeEntries(rootObject.get("advImports"), root.advImports))
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(currentInfoFile, updatedRoot)
        log.debug("Update local info file completed: file={}", currentInfoFile)
    }

    private fun lookupForInfo(
        localSrcInfo: Set<LocalSrcInfo>,
        codes: Set<String>,
        all: Boolean,
        excluded: Set<String>
    ): LookupResult<LocalSrcInfo> {
        return if (all) LookupResult(
            found = localSrcInfo.filter { !excluded.contains(it.code) }.toSet(),
            notFound = setOf(),
            duplicated = setOf()
        )
        else {
            val found: MutableSet<LocalSrcInfo> = mutableSetOf()
            val notFound: MutableSet<String> = mutableSetOf()
            codes.forEach { code ->
                if (!excluded.contains(code)) {
                    val info = localSrcInfo.find { it.code == code }
                    if (info != null) found.add(info)
                    else notFound.add(code)
                }
            }
            LookupResult(
                found = found,
                notFound = notFound,
                duplicated = setOf()
            )
        }
    }

    private fun mergeEntries(existingEntries: JsonNode?, incomingEntries: Set<LocalSrcInfo>): ArrayNode {
        val result = objectMapper.createArrayNode()
        val byCode = linkedMapOf<String, JsonNode>()

        existingEntries?.forEach { element ->
            val code = element.path("code").asText(null) ?: return@forEach
            byCode[code] = element
        }

        incomingEntries.forEach { info ->
            byCode[info.code] = objectMapper.valueToTree(info)
        }

        byCode.values.forEach { result.add(it) }
        return result
    }
}
