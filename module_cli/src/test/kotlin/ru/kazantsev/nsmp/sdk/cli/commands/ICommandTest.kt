package ru.kazantsev.nsmp.sdk.cli.commands

interface ICommandTest {
    val commandName: String

    fun checkExists()
    fun checkCliConnectorParamsDirect()
    fun checkCliConnectorParamsByConfigFileInPath()
    fun checkCliConnectorParamsByConfigFile()
    fun checkEmptyExecution()
    fun checkScriptsExecution()
    fun checkModulesExecution()
    fun checkFullExecution()
}
