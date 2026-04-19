@file:Suppress("SameParameterValue")

package ru.kazantsev.nsmp.sdk.cli

import org.junit.jupiter.api.BeforeEach
import ru.kazantsev.nsmp.sdk.cli.commands.CommandArgs
import ru.kazantsev.nsmp.sdk.runCli
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class CommandFunctionalTestBase {

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
        fullArgs += splitOptionArgument(CommandArgs.LOG_LEVEL.withValue("debug"))
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
        val scriptsRoot = testProjectDir.resolve("src/main/scripts")
        val found = Files.walk(scriptsRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy"
            }
        }
        kotlin.test.assertTrue(found, "Expected script file for code=$code in $scriptsRoot")
    }

    protected fun assertPulledModuleExists(code: String) {
        val modulesRoot = testProjectDir.resolve("src/main/modules")
        val found = Files.walk(modulesRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy"
            }
        }
        kotlin.test.assertTrue(found, "Expected module file for code=$code in $modulesRoot")
    }

    protected fun assertPulledAdvImportExists(code: String) {
        val advImportsRoot = testProjectDir.resolve(SrcFoldersParams.getDefaultAdvImportsRelativePathString())
        val found = Files.walk(advImportsRoot).use { pathStream ->
            pathStream.anyMatch { path ->
                Files.isRegularFile(path) && path.fileName.toString() == "$code.xml"
            }
        }
        kotlin.test.assertTrue(found, "Expected adv import file for code=$code in $advImportsRoot")
    }

    protected fun createLocalScript(code: String) {
        val scriptPath = testProjectDir.resolve("src/main/scripts/ru/kazantsev/demo/$code.groovy")
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
        val modulePath = testProjectDir.resolve("src/main/modules/ru/kazantsev/demo/$code.groovy")
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
        val advImportPath = testProjectDir.resolve("src/main/resources/advImports/$code.xml")
        Files.createDirectories(advImportPath.parent)
        Files.writeString(
            advImportPath,
            """
            <import code="$code"/>
            """.trimIndent()
        )
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
