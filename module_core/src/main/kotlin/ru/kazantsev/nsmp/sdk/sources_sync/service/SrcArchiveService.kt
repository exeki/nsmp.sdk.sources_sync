package ru.kazantsev.nsmp.sdk.sources_sync.service

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfoFileRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.InfoFileNotFound
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.ScriptTextNotFound
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Сервис для работы с архивом `src`: упаковка, распаковка и преобразование checksum-ответа.
 */
class SrcArchiveService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    companion object {
        private const val SRC_PUSH_ARCHIVE_ROOT = "src/main/groovy/ru/naumen"
    }

    /**
     * Распаковывает архив с исходниками в DTO с текстами и метаданными исходников.
     */
    fun unpackSrcArchive(srcArchive: ByteArray): SrcSetRoot<RemoteSrcTextInfo> {
        log.debug("Unpack archive started: size={} bytes", srcArchive.size)
        val scriptTexts = mutableMapOf<String, String>()
        val moduleTexts = mutableMapOf<String, String>()
        val advImportTexts = mutableMapOf<String, String>()
        var info: RemoteInfoFileRoot? = null

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
                        info = json.decodeFromString(String(zis.readBytes(), Charsets.UTF_8))
                    }
                }
                entry = zis.nextEntry
            }
        }

        val srcInfo = info ?: throw InfoFileNotFound()

        val result = SrcSetRoot(
            scripts = srcInfo.scripts.map {
                RemoteSrcTextInfo(
                    info = it,
                    text = scriptTexts[it.code] ?: throw ScriptTextNotFound(it.code, SrcType.SCRIPT)
                )
            }.toSet(),
            modules = srcInfo.modules.map {
                RemoteSrcTextInfo(
                    info = it,
                    text = moduleTexts[it.code] ?: throw ScriptTextNotFound(it.code, SrcType.MODULE)
                )
            }.toSet(),
            advImports = srcInfo.advImports.map {
                RemoteSrcTextInfo(
                    info = it,
                    text = advImportTexts[it.code] ?: throw ScriptTextNotFound(it.code, SrcType.ADV_IMPORT)
                )
            }.toSet()
        )
        log.debug(
            "Unpack archive completed: scripts={}, modules={}, advImports={}",
            result.scripts.size,
            result.modules.size,
            result.advImports.size
        )
        return result
    }

    /**
     * Собирает zip-архив из локальных source root.
     */
    fun <T : ILocalFile> buildSrcArchive(
        root: SrcSetRoot<T>,
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
                SrcFormat.GROOVY.code,
                "$SRC_PUSH_ARCHIVE_ROOT/scripts",
                root.scripts
            )
            writeSourcesToArchive(
                zipOutputStream,
                SrcFormat.GROOVY.code,
                "$SRC_PUSH_ARCHIVE_ROOT/modules",
                root.modules
            )
            writeSourcesToArchive(
                zipOutputStream,
                SrcFormat.XML.code,
                "$SRC_PUSH_ARCHIVE_ROOT/scripts/advimport",
                root.advImports
            )
        }

        val archive = outputStream.toByteArray()
        log.debug("Build archive completed: size={} bytes", archive.size)
        return archive
    }

    private fun <T : ILocalFile> writeSourcesToArchive(
        zipOutputStream: ZipOutputStream,
        format: String,
        archiveRoot: String,
        sources: SrcSet<T>
    ) {
        sources.forEach { source ->
            val entryName = "$archiveRoot/${source.code}.${format}"

            zipOutputStream.putNextEntry(ZipEntry(entryName))
            source.file.inputStream().use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
    }
}
