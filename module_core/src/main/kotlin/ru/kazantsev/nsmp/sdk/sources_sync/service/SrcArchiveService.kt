package ru.kazantsev.nsmp.sdk.sources_sync.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Сервис для работы с архивом `src`: упаковка, распаковка и преобразование checksum-ответа.
 */
class SrcArchiveService(
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val SRC_PUSH_ARCHIVE_ROOT = "src/main/groovy/ru/naumen"
    }

    /**
     * Собирает zip-архив из локальных source root.
     */
    fun buildSrcArchive(
        root: SrcFileDtoRoot,
        scriptsSrcFolder: SrcFolder,
        modulesSrcFolder: SrcFolder,
        advImportsSrcFolder: SrcFolder,
    ): ByteArray {
        log.debug(
            "Build archive started: scripts={}, modules={}, advImports={}",
            root.scripts.size,
            root.modules.size,
            root.advImports.size
        )
        val outputStream = ByteArrayOutputStream()

        ZipOutputStream(outputStream).use { zipOutputStream ->
            writeSourcesToArchive(
                zipOutputStream,
                scriptsSrcFolder,
                "$SRC_PUSH_ARCHIVE_ROOT/scripts",
                root.scripts
            )
            writeSourcesToArchive(
                zipOutputStream,
                modulesSrcFolder,
                "$SRC_PUSH_ARCHIVE_ROOT/modules",
                root.modules
            )
            writeSourcesToArchive(
                zipOutputStream,
                advImportsSrcFolder,
                "$SRC_PUSH_ARCHIVE_ROOT/scripts/advimport",
                root.advImports
            )
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
        val advImportTexts = mutableMapOf<String, String>()
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

                    normalizedEntryName.startsWith("advImports/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        advImportTexts[code] = String(zis.readBytes(), Charsets.UTF_8)
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
                    text = moduleTexts[it.code] ?: throw Exception("Module text ${it.code} not found")
                )
            },
            advImports = srcInfo.advImports.map {
                SrcDto(
                    info = it,
                    text = advImportTexts[it.code] ?: throw Exception("AdvImport text ${it.code} not found")
                )
            }
        )
        log.debug(
            "Unpack archive completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    private fun writeSourcesToArchive(
        zipOutputStream: ZipOutputStream,
        srcFolder: SrcFolder,
        archiveRoot: String,
        sources: List<SrcFileDto>
    ) {
        sources.forEach { source ->
            //val relativePath = srcFolder.getPath().toPath().relativize(source.file.toPath()).toString().replace(File.separatorChar, '/')
            val entryName = "$archiveRoot/${source.code}/${srcFolder.format}"

            zipOutputStream.putNextEntry(ZipEntry(entryName))
            source.file.inputStream().use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
    }
}
