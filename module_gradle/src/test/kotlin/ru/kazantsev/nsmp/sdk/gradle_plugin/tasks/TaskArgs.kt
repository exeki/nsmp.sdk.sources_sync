package ru.kazantsev.nsmp.sdk.gradle_plugin.tasks

import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams

enum class TaskArgs(
    val option: String,
    val defaultValue: String
) {
    INSTALLATION_ID("installationId", "EXEKI1"),
    CONFIG_PATH("configPath", ConnectorParams.getDefaultParamsFilePath()),
    SCHEME("scheme", "https"),
    HOST("host", "nsd1.exeki.local"),
    ACCESS_KEY("accessKey", ConnectorParams.byConfigFile(INSTALLATION_ID.defaultValue).accessKey),
    IGNORE_SSL("ignoreSsl", "true"),
    SCRIPTS("scripts", "testScript1,testScript2"),
    MODULES("modules", "testModule1,testModule2"),
    FORCE("force", "false");

    val flag: String
        get() = "--$option"

    fun withValue(value: String): String = "$flag=$value"

    fun withDefaultValue(): String = defaultValue.let(::withValue)

    fun requireDefaultValue(): String = defaultValue
}
