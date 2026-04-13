package ru.kazantsev.nsmp.sdk.sources_sync.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.io.File
import java.nio.file.Path

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class SrcStorageService(
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
    fun getInfoFile(): File = infoFilePath

    /**
     * Читает локальную информацию об исходниках.
     *
     * При необходимости фильтрует данные по кодам scripts/modules.
     */
    fun readLocalSrcInfo(req : SrcRequest): SrcInfoRoot {
        log.debug("Read local info: file={}, scriptsFilter={}, modulesFilter={}", infoFilePath, req.scripts.size, req.modules.size)
        if (!infoFilePath.exists() || infoFilePath.readText().isBlank()) {
            log.debug("Local info file not found or empty")
            return SrcInfoRoot()
        }

        val srcInfo = objectMapper.readValue(infoFilePath, SrcInfoRoot::class.java)

        val filtered = SrcInfoRoot(
            scripts = srcInfo.scripts.filter { (req.allScripts || (it.code in req.scripts)) && it.code !in req.scriptsExcluded},
            modules = srcInfo.modules.filter { (req.allModules || it.code in req.modules) && it.code !in req.modulesExcluded },
            advImports = srcInfo.advImports.filter { (req.allAdvImports || it.code in req.advImports) && it.code !in req.advImportsExcluded }
        )
        log.debug("Local info filtered: scripts={}, modules={}, advImports={}", filtered.scripts.size, filtered.modules.size, filtered.advImports.size)
        return filtered
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(root : SrcInfoRoot) {
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

        val rootObject = if (currentInfoFile.readText().isBlank()) {
            objectMapper.createObjectNode()
        } else {
            objectMapper.readTree(currentInfoFile) as ObjectNode
        }

        val updatedRoot = objectMapper.createObjectNode().apply {
            set<ArrayNode>("scripts", mergeEntries(rootObject.get("scripts"), root.scripts))
            set<ArrayNode>("modules", mergeEntries(rootObject.get("modules"), root.modules))
            set<ArrayNode>("advImports", mergeEntries(rootObject.get("advImports"), root.advImports))
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(currentInfoFile, updatedRoot)
        log.debug("Update local info file completed: file={}", currentInfoFile)
    }

    private fun mergeEntries(existingEntries: JsonNode?, incomingEntries: List<SrcInfo>): ArrayNode {
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
