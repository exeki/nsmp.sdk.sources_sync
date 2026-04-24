package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.req.SrcSetRequest
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Описывает один source set проекта и операции с ним.
 */
class SrcFolder(
    /**
     * Абсолютный путь до папки
     */
    val projectPath: Path,
    val relativePathString: String,
    val type: SrcType,
) {

    val absolutePath: Path = projectPath.resolve(relativePathString).normalize()

    val file: File = absolutePath.toFile()

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
    }

    private fun getInfo(remoteInfo: RemoteInfo): LocalInfo {
        return LocalInfo(
            checksum = remoteInfo.checksum,
            code = remoteInfo.code,
            lastSync = LocalDateTime.now().format(dateFormatter)
        )
    }

    /**
     * Записать новый файл исходника.
     * @param src ДТО файла
     */
    fun writeSourceFile(src: RemoteSrcTextInfo): LocalFileInfo {
        val packageDirectory = resolvePackageDirectory(src.text)
        packageDirectory.toFile().mkdirs()
        val sourceFile = packageDirectory.resolve("${src.info.code}.${type.format.code}")
        val file = sourceFile.toFile()
        file.writeText(src.text)
        log.debug("Source file written: file={}", sourceFile)
        return LocalFileInfo(file = file, info = getInfo(src.info), code = src.info.code)
    }

    /**
     * Найти файлы исходников по кодам в source set (независимо от вложенности по папкам).
     * @param includedCodes список кодов исходников
     */
    fun findSourceFiles(req: SrcSetRequest): SrcLookupResult<LocalFile> {
        val includedCodes = req.includedCodes.filter { it !in req.excludedCodes }
        log.debug("Find source files started: path={}, requested={}", absolutePath, includedCodes.size)
        val allFiles = absolutePath.toFile().walkTopDown().filter { it.isFile }.toList()
        val notFound = mutableSetOf<String>()
        val duplicated = mutableSetOf<String>()
        val found = mutableSetOf<LocalFile>()
        includedCodes.forEach { srcCode ->
            val matches = allFiles.filter {
                val excl = req.excludedCodes.contains(it.nameWithoutExtension)
                val incl = req.all || req.includedCodes.contains(it.nameWithoutExtension)
                !excl && incl
            }
            when (matches.size) {
                0 -> notFound.add(srcCode)
                1 -> found.add(LocalFile(code = srcCode, file = file))
                else -> duplicated.add(srcCode)
            }
        }
        log.debug(
            "Find source files completed: found={}, notFound={}, duplicated={}",
            found.size,
            notFound.size,
            duplicated.size
        )
        return SrcLookupResult(
            found = found,
            notFound = notFound,
            duplicated = duplicated,
            type = type
        )
    }

    /**
     * Получить все файлы исходников из папки.
     */
    fun getAllSourceFiles(): SrcLookupResult<LocalFile> {
        return findSourceFiles(SrcSetRequest(all = true, type = type))
    }

    /**
     * Определить package исходника, чтобы сохранить его в корректной папке.
     * @param sourceText текст файла, там будем искать package
     */
    private fun resolvePackageDirectory(sourceText: String): Path {
        if (!type.format.hasPackage) return absolutePath
        val packageName = PACKAGE_DECLARATION_REGEX.find(sourceText)?.groupValues?.get(1) ?: return absolutePath
        return absolutePath.resolve(packageName.replace('.', File.separatorChar))
    }
}
