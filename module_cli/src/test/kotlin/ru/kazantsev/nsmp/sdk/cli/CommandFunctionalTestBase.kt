@file:Suppress("SameParameterValue")

package ru.kazantsev.nsmp.sdk.cli

import org.junit.jupiter.api.BeforeEach
import ru.kazantsev.nsmp.sdk.cli.commands.CommandArgs
import ru.kazantsev.nsmp.sdk.runCli
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class CommandFunctionalTestBase {

    val LOG_LEVEL = "info"

    data class CliResult(
        val exitCode: Int,
        val stderr: String
    )

    companion object {
        private const val CLEAN_TEST_PROJECT_DIR = true
    }

    lateinit var testProjectDir: Path

    @BeforeEach
    fun setUpTestProject() {
        testProjectDir = Paths.get("build", "functional-cli-test-project")
        if (CLEAN_TEST_PROJECT_DIR) {
            deleteRecursively(testProjectDir)
        }
        Files.createDirectories(testProjectDir)
    }

    protected fun runCommand(
        command: String,
        vararg args: String
    ): CliResult {
        val fullArgs = mutableListOf<String>()
        fullArgs += command
        fullArgs += splitOptionArgument(CommandArgs.PROJECT_PATH.withValue(testProjectDir.toString()))
        fullArgs += splitOptionArgument(CommandArgs.LOG_LEVEL.withValue(LOG_LEVEL))
        args.forEach { arg ->
            fullArgs += splitOptionArgument(arg)
        }

        val errBytes = ByteArrayOutputStream()
        val code = runCli(fullArgs.toTypedArray(), PrintStream(errBytes, true, Charsets.UTF_8))
        return CliResult(
            code,
            errBytes.toString(Charsets.UTF_8)
        )
    }

    private fun splitOptionArgument(arg: String): List<String> {
        if (!arg.startsWith("--")) return listOf(arg)
        val equalsIndex = arg.indexOf('=')
        if (equalsIndex <= 2 || equalsIndex == arg.lastIndex) return listOf(arg)
        val option = arg.substring(0, equalsIndex)
        val value = arg.substring(equalsIndex + 1)
        return listOf(option, value)
    }

    protected fun directConnectorArgs(): Array<String> = arrayOf(
        CommandArgs.INSTALLATION_ID.withDefaultValue(),
        CommandArgs.SCHEME.withDefaultValue(),
        CommandArgs.HOST.withDefaultValue(),
        CommandArgs.ACCESS_KEY.withDefaultValue(),
        CommandArgs.IGNORE_SSL.withDefaultValue()
    )

    protected fun connectorArgsByConfigFileInPath(): Array<String> = arrayOf(
        CommandArgs.INSTALLATION_ID.withDefaultValue(),
        CommandArgs.CONFIG_PATH.withDefaultValue()
    )

    protected fun connectorArgsByConfigFile(): Array<String> = arrayOf(
        CommandArgs.INSTALLATION_ID.withDefaultValue()
    )

    protected fun assertPulledScriptExists(code: String) {
        val scriptsRoot = testProjectDir.resolve(SrcFoldersParams.getDefaultRelativeScriptsPathString())
        val found = Files.walk(scriptsRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy"
            }
        }
        kotlin.test.assertTrue(found, "Expected script file for code=$code in $scriptsRoot")
    }

    protected fun assertPulledModuleExists(code: String) {
        val modulesRoot = testProjectDir.resolve(SrcFoldersParams.getDefaultModulesRelativePathString())
        val found = Files.walk(modulesRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy"
            }
        }
        kotlin.test.assertTrue(found, "Expected module file for code=$code in $modulesRoot")
    }

    protected fun assertPulledAdvImportExists(code: String) {
        val advImportsRoot = testProjectDir.resolve(SrcFoldersParams.getDefaultAdvImportsRelativePathString())
        val advImportExtension = SrcType.ADV_IMPORT.format.code
        val found = Files.walk(advImportsRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.$advImportExtension"
            }
        }
        kotlin.test.assertTrue(found, "Expected adv import file for code=$code in $advImportsRoot")
    }

    protected fun createLocalScript(code: String) {
        val scriptPath = testProjectDir
            .resolve(SrcFoldersParams.getDefaultRelativeScriptsPathString())
            .resolve("ru/kazantsev/demo/$code.groovy")
        Files.createDirectories(scriptPath.parent)
        Files.writeString(
            scriptPath,
            """
            package ru.kazantsev.demo

            class $code {}
            """.trimIndent()
        )
    }

    protected fun createLocalModule(code: String) {
        val modulePath = testProjectDir
            .resolve(SrcFoldersParams.getDefaultModulesRelativePathString())
            .resolve("ru/kazantsev/demo/$code.groovy")
        Files.createDirectories(modulePath.parent)
        Files.writeString(
            modulePath,
            """
            package ru.kazantsev.demo

            class $code {}
            """.trimIndent()
        )
    }

    protected fun createLocalAdvImport(code: String) {
        val advImportExtension = SrcType.ADV_IMPORT.format.code
        val advImportPath =
            testProjectDir.resolve("${SrcFoldersParams.getDefaultAdvImportsRelativePathString()}/$code.$advImportExtension")
        Files.createDirectories(advImportPath.parent)
        Files.writeString(
            advImportPath,
            """
            <import code="$code"/>
            """.trimIndent()
        )
    }

    protected fun createSyncCheckFixture(
        scripts: List<String> = emptyList(),
        modules: List<String> = emptyList(),
        advImports: List<String> = emptyList(),
        withInfoFile: Boolean = true
    ) {
        scripts.forEach(::createLocalScript)
        modules.forEach(::createLocalModule)
        advImports.forEach(::createLocalAdvImport)
        if (withInfoFile) {
            writeLocalInfoFile()
        }
    }

    protected fun writeLocalInfoFile() {
        val infoPath = testProjectDir.resolve(".nsmp_sdk/src_info.json")
        Files.createDirectories(infoPath.parent)
        Files.writeString(
            infoPath,
            """
            {
              "scripts": [
                {
                  "checksum": "local-test-script-checksum",
                  "code": "testScript1"
                }
              ],
              "modules": [
                {
                  "checksum": "local-test-module-checksum",
                  "code": "testModule1"
                }
              ],
              "advImports": [
                {
                  "checksum": "local-test-adv-import-checksum",
                  "code": "testImport1"
                }
              ]
            }
            """.trimIndent()
        )
    }

    private fun deleteRecursively(path: Path) {
        if (!Files.exists(path)) {
            return
        }
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }
}
