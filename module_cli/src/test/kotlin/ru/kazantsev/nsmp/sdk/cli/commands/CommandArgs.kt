package ru.kazantsev.nsmp.sdk.cli.commands

import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams

enum class CommandArgs(
    val option: String,
    val defaultValue: String? = null
) {
    INSTALLATION_ID("installationId", "EXEKI1"),
    CONFIG_PATH("configPath", ConnectorParams.getDefaultParamsFilePath()),
    SCHEME("scheme", "https"),
    HOST("host", "nsd1.exeki.local"),
    ACCESS_KEY("accessKey", ConnectorParams.byConfigFile(INSTALLATION_ID.defaultValue).accessKey),
    IGNORE_SSL("ignoreSsl", "true"),
    PROJECT_PATH("projectPath"),
    LOG_LEVEL("log-level", "info"),
    SCRIPTS("scripts", "testScript1,testScript2"),
    MODULES("modules", "testModule1,testModule2"),
    ADV_IMPORTS("advImports", "testImport1,testImport2"),
    SCRIPTS_EXCLUDED("scriptsExcluded", "testScript2"),
    MODULES_EXCLUDED("modulesExcluded", "testModule2"),
    ADV_IMPORTS_EXCLUDED("advImportsExcluded", "testImport2"),
    ALL_MODULES("allModules", "false"),
    ALL_SCRIPTS("allScripts", "false"),
    ALL_ADV_IMPORTS("allAdvImports", "false"),
    FORCE("force", "false");

    val flag: String
        get() = "--$option"

    fun withValue(value: String): String = "$flag=$value"

    fun withDefaultValue(): String = withValue(requireDefaultValue())

    fun requireDefaultValue(): String = defaultValue
        ?: throw IllegalStateException("Default value is not defined for $name")
}
