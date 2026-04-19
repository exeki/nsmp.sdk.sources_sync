package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDto
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import java.io.File
import java.nio.file.Path

/**
 * Описывает один source set проекта и операции с ним.
 */
class SrcFolder(
    /**
     * Абсолютный путь до папки
     */
    projectPath: Path,
    relativePathString: String,
    val format: String,
) {

    val absolutePath: Path = projectPath.resolve(relativePathString).normalize()

    val file: File = absolutePath.toFile()

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
    }

    /**
     * Записать новый файл исходника.
     * @param src ДТО файла
     */
    fun writeSourceFile(src: SrcDto) {
        val packageDirectory = resolvePackageDirectory(src.text)
        packageDirectory.toFile().mkdirs()
        val sourceFile = packageDirectory.resolve("${src.info.code}.$format")
        sourceFile.toFile().writeText(src.text)
        log.debug("Source file written: file={}", sourceFile)
    }

    /**
     * Получить исходнки по параметрами
     */
    fun findSourceFiles(srcCodes: List<String>, all: Boolean, excluded: List<String>? = null): List<SrcFileDto> {
        val list = if (all) getAllSourceFiles() else findSourceFiles(srcCodes)
        return if (!excluded.isNullOrEmpty()) list.filter { !excluded.contains(it.code) }
        else list
    }

    /**
     * Найти файлы исходников по кодам в source set (независимо от вложенности по папкам).
     * @param srcCodes список кодов исходников
     */
    private fun findSourceFiles(srcCodes: List<String>): List<SrcFileDto> {
        log.debug("Find source files started: path={}, requested={}", absolutePath, srcCodes.size)
        val allFiles = absolutePath.toFile().walkTopDown().filter { it.isFile }.toList()
        val result = srcCodes.map { srcCode ->
            val matches = allFiles.filter { it.name == "$srcCode.$format" }

            val file = when (matches.size) {
                0 -> throw IllegalStateException("Source file $srcCode not found in ${absolutePath}")
                1 -> matches.single()
                else -> throw IllegalStateException("Several files with code $srcCode found in ${absolutePath}")
            }

            SrcFileDto(srcCode, file)
        }
        log.debug("Find source files completed: found={}", result.size)
        return result
    }

    /**
     * Получить все файлы исходников из папки.
     */
    private fun getAllSourceFiles(): List<SrcFileDto> {
        val rootDirectory = absolutePath.toFile()
        if (!file.exists()) return emptyList()

        val groovyFiles = rootDirectory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".$format") }
            .sortedBy { rootDirectory.toPath().relativize(it.toPath()).toString() }
            .toList()

        val groupedByCode = groovyFiles.groupBy { it.name.substringBeforeLast(".$format") }
        val duplicatedCodes = groupedByCode.filterValues { it.size > 1 }.keys
        if (duplicatedCodes.isNotEmpty()) {
            throw IllegalStateException("Several files with code $duplicatedCodes found in ${absolutePath}")
        }

        val result = groovyFiles.map { file ->
            SrcFileDto(file.name.substringBeforeLast(".$format"), file)
        }
        log.debug("Collected all source files: path={}, count={}", rootDirectory, result.size)
        return result
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
