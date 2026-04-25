package ru.kazantsev.nsmp.sdk.sources_sync.service.local_src.storage

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.lookup.SrcLookupResult
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteTextInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.fs.SrcCodeValidationException
import ru.kazantsev.nsmp.sdk.sources_sync.exception.src.local.fs.SrcPathValidationException
import java.io.File
import java.nio.file.Path
import java.util.*

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

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
        private val SOURCE_CODE_REGEX = Regex("^[A-Za-z0-9._-]{1,128}$")
        private val WINDOWS_RESERVED_FILE_NAMES = setOf(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
        )
    }

    /**
     * Записать новый файл исходника.
     * @param src ДТО файла
     */
    fun writeSourceFile(src: RemoteTextInfo): LocalFileInfo {
        validateSourceCode(src.info.code)
        val packageDirectory = resolvePackageDirectory(src.text)
        packageDirectory.toFile().mkdirs()
        val sourceFile = packageDirectory.resolve("${src.info.code}.${type.format.code}").normalize()
        validateResolvedPath(sourceFile)
        val file = sourceFile.toFile()
        file.parentFile.mkdirs()
        file.writeText(src.text)
        log.debug("Source file written: file={}", sourceFile)
        return LocalFileInfo(
            file = file,
            code = src.info.code,
            info = LocalInfo(
                checksum = src.info.checksum,
                code = src.info.code
            )
        )
    }

    /**
     * Получить все файлы исходников из папки.
     */
    fun lookupLocalFiles(req: SrcSetRequest): SrcLookupResult<LocalFile> {
        if (req.isEmpty()) return SrcLookupResult.empty(type = type)
        val requestedCodes = req.includedCodes.filter { it !in req.excludedCodes }.toSet()
        log.debug(
            "Find source files fixed started: path={}, requested={}, all={}",
            absolutePath,
            requestedCodes.size,
            req.all
        )

        if (!file.exists()) {
            log.debug("Find source files fixed completed: source path does not exist")
            return SrcLookupResult.Companion.empty(type)
        }

        val allFiles = file.walkTopDown().filter { it.isFile }.toList()
        val eligibleFiles = allFiles.filter { candidate ->
            candidate.nameWithoutExtension !in req.excludedCodes &&
                    candidate.extension == type.format.code
        }
        val result: SrcLookupResult<LocalFile>

        if (req.all) result = SrcLookupResult(
            notFound = emptySet(),
            duplicated = emptySet(),
            type = type,
            found = eligibleFiles.map { matchedFile ->
                LocalFile(
                    code = matchedFile.nameWithoutExtension,
                    file = matchedFile
                )
            }.toSet()
        )
        else {
            val notFound = mutableSetOf<String>()
            val duplicated = mutableSetOf<String>()
            val found = mutableSetOf<LocalFile>()
            requestedCodes.forEach { srcCode ->
                val matches = eligibleFiles.filter { matchedFile ->
                    matchedFile.nameWithoutExtension == srcCode
                }
                when (matches.size) {
                    0 -> notFound.add(srcCode)
                    1 -> found.add(LocalFile(code = srcCode, file = matches.single()))
                    else -> duplicated.add(srcCode)
                }
            }
            result = SrcLookupResult(
                found = found,
                notFound = notFound,
                duplicated = duplicated,
                type = type
            )
        }
        log.debug(
            "Find source files fixed completed: found={}, notFound={}, duplicated={}",
            result.found.size,
            result.notFound.size,
            result.duplicated.size
        )
        return result
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

    private fun validateSourceCode(code: String) {
        if (code.isBlank()) throw SrcCodeValidationException("Source code must not be blank")
        if (!SOURCE_CODE_REGEX.matches(code)) {
            throw SrcCodeValidationException("Source code contains forbidden characters: \"$code\"")
        }
        if (code.contains("..")) {
            throw SrcCodeValidationException("Source code must not contain '..': \"$code\"")
        }
        if (code.contains('/') || code.contains('\\')) {
            throw SrcCodeValidationException("Source code must not contain path separators: \"$code\"")
        }
        val fileNameHead = code.substringBefore('.').uppercase(Locale.ROOT)
        if (fileNameHead in WINDOWS_RESERVED_FILE_NAMES) {
            throw SrcCodeValidationException(
                "Source code resolves to a reserved file name on Windows: \"$code\""
            )
        }
    }

    private fun validateResolvedPath(path: Path) {
        val normalizedRoot = absolutePath.normalize()
        val normalizedPath = path.normalize()
        if (!normalizedPath.startsWith(normalizedRoot)) {
            throw SrcPathValidationException(
                "Resolved source path escapes source root: \"$normalizedPath\""
            )
        }
    }
}
