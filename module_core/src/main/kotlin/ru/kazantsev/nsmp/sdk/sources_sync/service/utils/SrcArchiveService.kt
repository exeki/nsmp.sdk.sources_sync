package ru.kazantsev.nsmp.sdk.sources_sync.service.utils

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.dto.RemoteInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.Root
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcTextChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.InfoFileNotFound
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.remote.RemoteSrcTextNotFound
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
        private val SRC_PUSH_ARCHIVE_ROOT_SRC_PATH_ROOT = Root.byEnumIterator {
            when (it) {
                SrcType.SCRIPT -> "$SRC_PUSH_ARCHIVE_ROOT/scripts"
                SrcType.MODULE -> "$SRC_PUSH_ARCHIVE_ROOT/modules"
                SrcType.ADV_IMPORT -> "$SRC_PUSH_ARCHIVE_ROOT/scripts/advimport"
            }
        }
    }

    private val conversionService : ConversionService = ConversionService()

    /**
     * Распаковывает архив с исходниками в DTO с текстами и метаданными исходников.
     */
    fun unpackSrcArchive(srcArchive: ByteArray): SrcSetRoot<SrcTextChecksum> {
        log.debug("Unpack archive started: size={} bytes", srcArchive.size)
        val textsRoot = Root.byEnumIterator { mutableMapOf<String, String>() }
        var info: RemoteInfoRoot? = null

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
                        textsRoot[SrcType.MODULE][code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName.startsWith("scripts/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        textsRoot[SrcType.SCRIPT][code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName.startsWith("advImports/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        textsRoot[SrcType.ADV_IMPORT][code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName == "info.json" -> {
                        info = json.decodeFromString(String(zis.readBytes(), Charsets.UTF_8))
                    }
                }
                entry = zis.nextEntry
            }
        }

        val remoteSrcInfo = info ?: throw InfoFileNotFound()
        val remoteInfoRoot = conversionService.convertRemoteInfoRoot(remoteSrcInfo)
        val result = SrcSetRoot.byEnumIterator { type ->
            remoteInfoRoot[type].map {
                SrcTextChecksum(
                    code = it.code,
                    checksum = it.checksum,
                    text = textsRoot[type][it.code] ?: throw RemoteSrcTextNotFound(it.code, SrcType.SCRIPT)
                )
            }.toSet()
        }
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
    fun <T : ISrcFile> buildSrcArchive(
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
            root.forEach { (type, ts) ->
                writeSourcesToArchive(
                    zipOutputStream,
                    type.format.code,
                    SRC_PUSH_ARCHIVE_ROOT_SRC_PATH_ROOT[type],
                    ts
                )
            }
        }
        val archive = outputStream.toByteArray()
        log.debug("Build archive completed: size={} bytes", archive.size)
        return archive
    }

    private fun <T : ISrcFile> writeSourcesToArchive(
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
