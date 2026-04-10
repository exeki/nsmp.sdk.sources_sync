package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin.tasks

interface ITaskTest {

    /**
     * Наименование задачи, взять из статик поля NAME задачи
     */
    val taskName : String

    /**
     * Проверить что задача и ее аргументы существуют
     */
    fun checkExists()

    /**
     * Вызвать задачу и передать через cli все данные для подключения через cli
     */
    fun checkCliConnectorParamsDirect()

    /**
     * Вызвать задачу и передать в нее через cli путь до конфигурационного файла и ИД инсталляции
     */
    fun checkCliConnectorParamsByConfigFileInPath()

    /**
     * Вызвать задачу и передать в нее через cli только ИД инсталляции
     */
    fun checkCliConnectorParamsByConfigFile()

    /**
     * Вызвать задачу, передать все параметры для подключения через extension gradle
     */
    fun checkExtensionConnectorParamsDirect()

    /**
     * Вызвать задачу, передать путь до конфигурационного файла и ИД инсталляции для подключения через extension gradle
     */
    fun checkExtensionConnectorParamsByConfigFileInPath()

    /**
     * Вызвать задачу, передать ИД инсталляции для подключения через extension gradle
     */
    fun checkExtensionConnectorParamsByConfigFile()

    /**
     * Задать installationId через extension, вызвать задачу ничего в нее не передавая
     */
    fun checkEmptyExecution()

    /**
     * Задать installationId через extension, вызвать задачу передав в нее пару кодов скриптов
     */
    fun checkScriptsExecution()

    /**
     * Задать installationId через extension, вызвать задачу передав в нее пару кодов модулей
     */
    fun checkModulesExecution()

    /**
     * Задать installationId через extension, вызвать задачу передав в нее пару кодов модулей и пару кодов скриптов
     */
    fun checkFullExecution()

    /**
     * Передать в запрос параметр allModules - true
     */
    fun checkAllModulesExecution()

    /**
     * Передать в запрос параметр allScripts - true
     */
    fun checkAllScriptsExecution()

    /**
     * Передать в запрос параметр allAdvImports - true
     */
    fun checkAllAdvImportsExecution()
}