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
    ACCESS_KEY("accessKey", "69f8d9c0-56bd-4c87-8010-4a95e2cb4b14"),
    IGNORE_SSL("ignoreSsl", "true"),
    PROJECT_PATH("projectPath"),
    LOG_LEVEL("log-level", "info"),
    SCRIPTS("scripts", "testScript1,testScript2"),
    MODULES("modules", "testModule1,testModule2"),
    FORCE("force", "true");

    val flag: String
        get() = "--$option"

    fun withValue(value: String): String = "$flag=$value"

    fun withDefaultValue(): String = withValue(requireDefaultValue())

    fun requireDefaultValue(): String = defaultValue
        ?: throw IllegalStateException("Default value is not defined for $name")
}
