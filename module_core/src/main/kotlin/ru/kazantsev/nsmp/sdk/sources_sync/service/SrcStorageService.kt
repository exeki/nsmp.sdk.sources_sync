package ru.kazantsev.nsmp.sdk.sources_sync.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
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
        private const val SDK_DIR_PATH = ".smp_sdk"
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
    fun readLocalSrcInfo(
        scriptsFilter: Collection<String> = emptyList(),
        modulesFilter: Collection<String> = emptyList()
    ): SrcInfoRoot {
        log.debug("Read local info: file={}, scriptsFilter={}, modulesFilter={}", infoFilePath, scriptsFilter.size, modulesFilter.size)
        if (!infoFilePath.exists() || infoFilePath.readText().isBlank()) {
            log.debug("Local info file not found or empty")
            return SrcInfoRoot()
        }

        val srcInfo = objectMapper.readValue(infoFilePath, SrcInfoRoot::class.java)
        if (scriptsFilter.isEmpty() && modulesFilter.isEmpty()) {
            log.debug("Local info loaded: scripts={}, modules={}", srcInfo.scripts.size, srcInfo.modules.size)
            return srcInfo
        }

        val filtered = SrcInfoRoot(
            scripts = srcInfo.scripts.filter { scriptsFilter.isEmpty() || it.code in scriptsFilter },
            modules = srcInfo.modules.filter { modulesFilter.isEmpty() || it.code in modulesFilter }
        )
        log.debug("Local info filtered: scripts={}, modules={}", filtered.scripts.size, filtered.modules.size)
        return filtered
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(scripts: List<SrcInfo>, modules: List<SrcInfo>) {
        log.debug("Update local info file started: scripts={}, modules={}", scripts.size, modules.size)
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
            set<ArrayNode>("scripts", mergeEntries(rootObject.get("scripts"), scripts))
            set<ArrayNode>("modules", mergeEntries(rootObject.get("modules"), modules))
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
