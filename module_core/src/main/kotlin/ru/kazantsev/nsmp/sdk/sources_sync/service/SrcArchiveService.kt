package ru.kazantsev.nsmp.sdk.sources_sync.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Сервис для работы с архивом `src`: упаковка, распаковка и преобразование checksum-ответа.
 */
class SrcArchiveService (
    private val objectMapper : ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val SRC_PUSH_ARCHIVE_ROOT = "src/main/groovy/ru/naumen"
    }

    /**
     * Собирает zip-архив из локальных source root.
     */
    fun buildSrcArchive(
        scripts: List<SrcFileDto>,
        modules: List<SrcFileDto>,
        scriptsSrcFolder: SrcFolder,
        modulesSrcFolder: SrcFolder
    ): ByteArray {
        log.debug("Build archive started: scripts={}, modules={}", scripts.size, modules.size)
        val outputStream = ByteArrayOutputStream()

        ZipOutputStream(outputStream).use { zipOutputStream ->
            writeSourcesToArchive(zipOutputStream, scriptsSrcFolder, "$SRC_PUSH_ARCHIVE_ROOT/scripts", scripts)
            writeSourcesToArchive(zipOutputStream, modulesSrcFolder, "$SRC_PUSH_ARCHIVE_ROOT/modules", modules)
        }

        val archive = outputStream.toByteArray()
        log.debug("Build archive completed: size={} bytes", archive.size)
        return archive
    }

    /**
     * Распаковывает архив с исходниками в DTO с текстами и метаданными исходников.
     */
    fun unpackSrcArchive(srcArchive: ByteArray): SrcDtoRoot {
        log.debug("Unpack archive started: size={} bytes", srcArchive.size)
        val scriptTexts = mutableMapOf<String, String>()
        val moduleTexts = mutableMapOf<String, String>()
        var info: SrcInfoRoot? = null

        ZipInputStream(ByteArrayInputStream(srcArchive)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (entry.isDirectory) {
                    entry = zis.nextEntry
                    continue
                }

                val normalizedEntryName = entry.name.replace('\\', '/')

                when {
                    normalizedEntryName.startsWith("modules/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        moduleTexts[code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName.startsWith("scripts/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        scriptTexts[code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName == "info.json" -> {
                        info = objectMapper.readValue(
                            String(zis.readBytes(), Charsets.UTF_8),
                            SrcInfoRoot::class.java
                        )
                    }
                }
                entry = zis.nextEntry
            }
        }

        val srcInfo = info ?: throw Exception("File \"info.json\" not found")

        val result = SrcDtoRoot(
            scripts = srcInfo.scripts.map {
                SrcDto(
                    info = it,
                    text = scriptTexts[it.code] ?: throw Exception("Script text ${it.code} not found")
                )
            },
            modules = srcInfo.modules.map {
                SrcDto(
                    info = it,
                    text = moduleTexts[it.code] ?: throw Exception("Module test ${it.code} not found")
                )
            }
        )
        log.debug("Unpack archive completed: scripts={}, modules={}", result.scripts.size, result.modules.size)
        return result
    }

    private fun writeSourcesToArchive(
        zipOutputStream: ZipOutputStream,
        srcFolder: SrcFolder,
        archiveRoot: String,
        sources: List<SrcFileDto>
    ) {
        sources.forEach { source ->
            val relativePath = srcFolder.getPath().toPath().relativize(source.file.toPath()).toString()
                .replace(File.separatorChar, '/')
            val entryName = "$archiveRoot/$relativePath"

            zipOutputStream.putNextEntry(ZipEntry(entryName))
            source.file.inputStream().use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
    }
}
