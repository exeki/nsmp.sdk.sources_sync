package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks.TaskArgs
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.test.assertTrue

abstract class PluginFunctionalTestBase {

    companion object {
        const val CLEAN_TEST_PROJECT_DIR = true

        private const val TEMPLATE_SETTINGS = "templates/consumer/settings.gradle.kts.tpl"
        private const val TEMPLATE_BUILD = "templates/consumer/build.gradle.kts.tpl"
        private const val TEMPLATE_SDK_CONFIG_FULL = "templates/consumer/sdk-configuration-full.gradle.kts.tpl"
        private const val TEMPLATE_SDK_CONFIG_INSTALLATION_ONLY =
            "templates/consumer/sdk-configuration-installation-only.gradle.kts.tpl"
        private const val TEMPLATE_SDK_CONFIG_IN_PATH =
            "templates/consumer/sdk-configuration-in-path.gradle.kts.tpl"
        private const val TEMPLATE_LOCAL_INFO_JSON = "templates/consumer/local-info.json.tpl"
    }

    lateinit var testProjectDir: Path

    @BeforeEach
    fun setUpTestProject() {
        testProjectDir = Paths.get("build", "functional-test-project")
        if (CLEAN_TEST_PROJECT_DIR) deleteRecursively(testProjectDir)
        Files.createDirectories(testProjectDir)
    }

    protected fun writeConsumerProject() {
        writeConsumerProjectWithConfigTemplate(TEMPLATE_SDK_CONFIG_FULL)
    }

    protected fun writeConsumerProjectWithInstallationOnlyConfig() {
        writeConsumerProjectWithConfigTemplate(TEMPLATE_SDK_CONFIG_INSTALLATION_ONLY)
    }

    protected fun writeConsumerProjectWithConfigInPath() {
        writeConsumerProjectWithConfigTemplate(TEMPLATE_SDK_CONFIG_IN_PATH)
    }

    protected fun writeLocalInfoFile() {
        writeFileFromTemplate(".nsmp_sdk/src_info.json", TEMPLATE_LOCAL_INFO_JSON, emptyMap())
    }

    protected fun runner(vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(*arguments)
            .withPluginClasspath()
    }

    protected fun connectorParamsDirectArgs(): Array<String> {
        return arrayOf(
            TaskArgs.INSTALLATION_ID.withDefaultValue(),
            TaskArgs.SCHEME.withDefaultValue(),
            TaskArgs.HOST.withDefaultValue(),
            TaskArgs.ACCESS_KEY.withDefaultValue(),
            TaskArgs.IGNORE_SSL.withDefaultValue()
        ).toList().toTypedArray()
    }

    protected fun connectorParamsByConfigFileInPathArgs(): Array<String> {
        return arrayOf(
            TaskArgs.INSTALLATION_ID.withDefaultValue(),
            TaskArgs.CONFIG_PATH.withDefaultValue()
        ).toList().toTypedArray()
    }

    protected fun connectorParamsByConfigFileArgs(): Array<String> {
        return arrayOf(
            TaskArgs.INSTALLATION_ID.withDefaultValue()
        ).toList().toTypedArray()
    }

    private fun writeConsumerProjectWithConfigTemplate(sdkConfigTemplatePath: String) {
        val variables = mapOf(
            "TEST_INSTALLATION_ID" to TaskArgs.INSTALLATION_ID.requireDefaultValue(),
            "TEST_SCHEME" to TaskArgs.SCHEME.requireDefaultValue(),
            "TEST_HOST" to TaskArgs.HOST.requireDefaultValue(),
            "TEST_ACCESS_KEY" to TaskArgs.ACCESS_KEY.requireDefaultValue(),
            "IGNORE_SSL" to TaskArgs.IGNORE_SSL.requireDefaultValue(),
            "TEST_CONFIG_PATH" to escapeForKotlinString(TaskArgs.CONFIG_PATH.requireDefaultValue())
        )
        val sdkConfiguration = renderTemplate(loadTemplate(sdkConfigTemplatePath), variables)

        writeFileFromTemplate("settings.gradle.kts", TEMPLATE_SETTINGS, variables)
        writeFileFromTemplate(
            "build.gradle.kts",
            TEMPLATE_BUILD,
            variables + mapOf("SDK_CONFIGURATION" to sdkConfiguration)
        )
    }

    private fun writeFileFromTemplate(
        relativePath: String,
        templatePath: String,
        variables: Map<String, String>
    ) {
        val destinationPath = testProjectDir.resolve(relativePath)
        destinationPath.parent?.let { Files.createDirectories(it) }
        val rendered = renderTemplate(loadTemplate(templatePath), variables)
        Files.writeString(destinationPath, rendered)
    }

    private fun loadTemplate(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Template not found: $path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun renderTemplate(template: String, variables: Map<String, String>): String {
        var rendered = template
        variables.forEach { (key, value) ->
            rendered = rendered.replace("{{${key}}}", value)
        }
        return rendered
    }

    private fun escapeForKotlinString(value: String): String {
        return value.replace("\\", "\\\\")
    }

    protected fun assertPulledScriptExists(code: String) {
        val scriptsRoot = testProjectDir.resolve(SrcSyncService.getDefaultScriptsPath())
        val found = Files.walk(scriptsRoot).use { pathStream ->
            pathStream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy" }
        }
        assertTrue(found, "Expected script file for code=$code in $scriptsRoot")
    }

    protected fun assertPulledModuleExists(code: String) {
        val modulesRoot = testProjectDir.resolve(SrcSyncService.getDefaultModulesPath())
        val found = Files.walk(modulesRoot).use { pathStream ->
            pathStream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString() == "$code.groovy" }
        }
        assertTrue(found, "Expected module file for code=$code in $modulesRoot")
    }

    protected fun assertPulledAdvImportExists(code: String) {
        val resourcesRoot = testProjectDir.resolve(SrcSyncService.getDefaultAdvImportsPath())
        val found = Files.walk(resourcesRoot).use { pathStream ->
            pathStream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString() == "$code.xml" }
        }
        assertTrue(found, "Expected adv import file for code=$code in $resourcesRoot")
    }

    protected fun createLocalScript(code: String) {
        val scriptPath = testProjectDir.resolve(SrcSyncService.getDefaultScriptsPath()).resolve("ru/kazantsev/demo/$code.groovy")
        Files.createDirectories(scriptPath.parent)
        Files.writeString(
            scriptPath,
            """
            package ru.kazantsev.demo

            def code = "$code"
            def pushTome = "${LocalDateTime.now()}" 
            def a = 111
            """.trimIndent()
        )
    }

    protected fun createLocalModule(code: String) {
        val modulePath = testProjectDir.resolve(SrcSyncService.getDefaultModulesPath()).resolve("ru/kazantsev/demo/$code.groovy")
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
        val advImportPath = testProjectDir.resolve(SrcSyncService.getDefaultAdvImportsPath()).resolve("$code.xml")
        Files.createDirectories(advImportPath.parent)
        Files.writeString(
            advImportPath,
            """
            <import code="$code"/>
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
