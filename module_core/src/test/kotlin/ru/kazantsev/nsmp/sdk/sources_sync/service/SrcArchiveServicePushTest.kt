package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDtoRoot
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SrcArchiveServicePushTest {

    private lateinit var projectDir: Path

    @BeforeEach
    fun setUp() {
        projectDir = Paths.get("build", "core-test-push-one-script")
        deleteRecursively(projectDir)
        projectDir.createDirectories()
    }

    @Test
    fun pushTest() {
        val scriptsFolder = SrcFolder(projectDir.resolve("src/main/scripts"), "groovy")
        val modulesFolder = SrcFolder(projectDir.resolve("src/main/modules"), "groovy")
        val advImportsFolder = SrcFolder(projectDir.resolve("src/main/resources"), "xml")
        val archiveService = SrcArchiveService(
            scriptsSrcFolder = scriptsFolder,
            modulesSrcFolder = modulesFolder,
            advImportsSrcFolder = advImportsFolder
        )
        val scriptText = """
            package ru.kazantsev.demo

            class testScript1 {} 
            
            def a = 123
        """.trimIndent()
        val scriptPath = projectDir.resolve("src/main/scripts/ru/kazantsev/demo/testScript1.groovy")
        scriptPath.parent.createDirectories()
        Files.writeString(scriptPath, scriptText)

        val rootToPush = SrcFileDtoRoot(
            scripts = scriptsFolder.findSourceFiles(
                srcCodes = listOf("testScript1"),
                all = false
            )
        )
        val archiveBytes = archiveService.buildSrcArchive(rootToPush)

        val localArchivePath = projectDir.resolve("push-outgoing-archive.zip")
        localArchivePath.writeBytes(archiveBytes)

        assertTrue(localArchivePath.exists(), "Push archive should be saved on local machine")
        assertTrue(Files.size(localArchivePath) > 0, "Saved push archive should not be empty")

        val zipEntries = mutableListOf<String>()
        val savedArchiveBytes = localArchivePath.readBytes()
        ZipInputStream(savedArchiveBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                zipEntries += entry.name
                println("ZIP entry: ${entry.name}, size=${entry.size}")
                if (entry.name == "src/main/groovy/ru/naumen/scripts/testScript1.groovy") {
                    assertContentEquals(scriptText.toByteArray(), zis.readBytes())
                }
                entry = zis.nextEntry
            }
        }

        assertEquals(
            listOf("src/main/groovy/ru/naumen/scripts/testScript1.groovy"),
            zipEntries
        )

        //val scriptsChecksums = connector.pushScripts(srcArchiveService.buildSrcArchive(requestedRoot))
        //val pushedInfo = SrcInfoRoot.fromChecksums(scriptsChecksums)
        //srcStorageService.updateInfoFile(pushedInfo)
    }

    private fun deleteRecursively(path: Path) {
        if (!path.exists()) return
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }
}
