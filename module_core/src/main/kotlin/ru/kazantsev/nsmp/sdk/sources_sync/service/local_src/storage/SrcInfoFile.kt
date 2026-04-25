package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.storage

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import java.io.File
import java.nio.file.Path

class SrcInfoFile(
    val type: SrcType,
    @Suppress("CanBeParameter", "RedundantSuppression")
    private val projectPath: Path,
    @Suppress("CanBeParameter", "RedundantSuppression")
    private val fileName: String,
    private val json : Json
) {

    private val log = LoggerFactory.getLogger(javaClass)


    val absolutePath: Path =
        projectPath.resolve(Constants.SDK_DIR_PATH)
            .resolve(Constants.INFO_DIR)
            .resolve(fileName)
            .normalize()

    val file: File = absolutePath.toFile()

    /**
     * Читает локальный файл с информацией целиком
     */
    fun read(): SrcSet<LocalInfo> {
        log.debug("Read local info for type \"${type.code}\": file={}", absolutePath)
        if (!file.exists() || file.readText().isBlank()) {
            log.debug("Local info file for type \"${type.code}\" not found or empty")
            return SrcSet.empty(type)
        }
        val map: Map<String, LocalInfo> = json.decodeFromString(file.readText())
        return SrcSet(map, type)
    }

    private fun write(map: Map<String, LocalInfo>) {
        file.parentFile.mkdirs()
        file.writeText(json.encodeToString(map))
    }

    private fun write(srcSet: SrcSet<LocalInfo>) {
        write(srcSet.map)
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun update(set: SrcSet<LocalInfo>): SrcSet<LocalInfo> {
        if(set.isEmpty()) return set
        log.debug("Update local info file for type \"${type.code}\" started: size={}", set.size)
        val currentSet = read()
        val updatedRoot = mergeEntries(currentSet, set)
        write(updatedRoot)
        log.debug("Update local info file for type \"${type.code}\" completed: file={}", file)
        return set
    }

    fun lookupLocalInfo(req: SrcSetRequest): SrcLookupResult<LocalInfo> {
        val localSrcInfo = read()
        val result = if (req.all) SrcLookupResult(
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
        return result
    }

    @Suppress("unused")
    fun getLocalInfo(req: SrcSetRequest): SrcSet<LocalInfo> {
        val result = lookupLocalInfo(req)
        return result.convertToSrcSet()
    }

    private fun mergeEntries(
        existingEntries: SrcSet<LocalInfo>,
        incomingEntries: SrcSet<LocalInfo>
    ): SrcSet<LocalInfo> {
        val map = mutableMapOf<String, LocalInfo>()
        map.putAll(existingEntries.map)
        map.putAll(incomingEntries.map)
        return SrcSet(map, incomingEntries.type)
    }
}