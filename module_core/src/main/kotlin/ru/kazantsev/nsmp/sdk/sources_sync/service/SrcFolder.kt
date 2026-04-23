package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteSrcInfo
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
    val format: String,
) {

    val absolutePath: Path = projectPath.resolve(relativePathString).normalize()

    val file: File = absolutePath.toFile()

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
    }

    private fun getInfo(remoteInfo: RemoteSrcInfo): LocalSrcInfo {
        return LocalSrcInfo(
            checksum = remoteInfo.checksum,
            code = remoteInfo.code,
            lastSync = LocalDateTime.now().format(dateFormatter)
        )
    }

    /**
     * Записать новый файл исходника.
     * @param src ДТО файла
     */
    fun writeSourceFile(src: RemoteSrcTextInfo): LocalSrcFileInfo {
        val packageDirectory = resolvePackageDirectory(src.text)
        packageDirectory.toFile().mkdirs()
        val sourceFile = packageDirectory.resolve("${src.info.code}.$format")
        val file = sourceFile.toFile()
        file.writeText(src.text)
        log.debug("Source file written: file={}", sourceFile)
        return LocalSrcFileInfo(file = file, info = getInfo(src.info), code = src.info.code)
    }

    /**
     * Получить исходники по параметрам
     */
    fun findSourceFiles(srcCodes: Set<String>, all: Boolean, excluded: Set<String>): LookupResult<LocalSrcFile> {
        return if (all) getAllSourceFiles(excluded)
        else findSourceFiles(srcCodes.filter { it !in excluded })
    }

    /**
     * Найти файлы исходников по кодам в source set (независимо от вложенности по папкам).
     * @param srcCodes список кодов исходников
     */
    fun findSourceFiles(srcCodes: List<String>): LookupResult<LocalSrcFile> {
        log.debug("Find source files started: path={}, requested={}", absolutePath, srcCodes.size)
        val allFiles = absolutePath.toFile().walkTopDown().filter { it.isFile }.toList()
        val notFound = mutableSetOf<String>()
        val duplicated = mutableSetOf<String>()
        val found = mutableSetOf<LocalSrcFile>()
        srcCodes.forEach { srcCode ->
            val matches = allFiles.filter { it.name == "$srcCode.$format" }
            when (matches.size) {
                0 -> notFound.add(srcCode)
                1 -> found.add(LocalSrcFile(code = srcCode, file = file))
                else -> duplicated.add(srcCode)
            }
        }
        log.debug(
            "Find source files completed: found={}, notFound={}, duplicated={}",
            found.size,
            notFound.size,
            duplicated.size
        )
        return LookupResult(found = found, notFound = notFound, duplicated = duplicated)
    }

    /**
     * Получить все файлы исходников из папки.
     */
    fun getAllSourceFiles(excluded: Set<String>): LookupResult<LocalSrcFile> {
        val rootDirectory = absolutePath.toFile()
        if (!file.exists()) return LookupResult.empty()

        val groovyFiles = rootDirectory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".$format") }
            .sortedBy { rootDirectory.toPath().relativize(it.toPath()).toString() }
            .toList()

        val groupedByCode = groovyFiles.groupBy { it.name.substringBeforeLast(".$format") }
        val duplicated = groupedByCode.filterValues { it.size > 1 }.keys

        val result = groovyFiles
            .map { file -> LocalSrcFile(file.name.substringBeforeLast(".$format"), file) }
            .filter { !excluded.contains(it.code) }
        log.debug("Collected all source files: path={}, count={}", rootDirectory, result.size)
        return LookupResult(duplicated = duplicated, found = setOf(), notFound = setOf())
    }

    /**
     * Определить package исходника, чтобы сохранить его в корректной папке.
     * @param sourceText текст файла, там будем искать package
     */
    private fun resolvePackageDirectory(sourceText: String): Path {
        if (format != "groovy") return absolutePath
        val packageName = PACKAGE_DECLARATION_REGEX.find(sourceText)?.groupValues?.get(1) ?: return absolutePath
        return absolutePath.resolve(packageName.replace('.', File.separatorChar))
    }
}
