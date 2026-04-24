package ru.kazantsev.nsmp.sdk.sources_sync

import java.nio.file.Path

class SrcFoldersParams(
    private val projectAbsolutePath: String,
    private val scriptsRelativePath: String = getDefaultRelativeScriptsPathString(),
    private val modulesRelativePath: String = getDefaultModulesRelativePathString(),
    private val advImportsRelativePath: String = getDefaultAdvImportsRelativePathString(),
) {
    companion object {
        private const val DEFAULT_SCRIPTS_PATH: String = "src/main/scripts"
        private const val DEFAULT_MODULES_PATH: String = "src/main/modules"
        private const val DEFAULT_ADV_IMPORTS_PATH: String = "src/main/advimports"

        @JvmStatic
        fun getDefaultRelativeScriptsPathString() = DEFAULT_SCRIPTS_PATH

        @JvmStatic
        fun getDefaultModulesRelativePathString() = DEFAULT_MODULES_PATH

        @JvmStatic
        fun getDefaultAdvImportsRelativePathString() = DEFAULT_ADV_IMPORTS_PATH
    }

    fun getProjectAbsolutePathString(): String = projectAbsolutePath.replace("\\", "/")

    fun getProjectAbsolutePath(): Path = Path.of(getProjectAbsolutePathString())

    fun getScriptsRelativePathString(): String = scriptsRelativePath.replace("\\", "/")

    fun getModulesRelativePathString(): String = modulesRelativePath.replace("\\", "/")

    fun getAdvImportsRelativePathString(): String = advImportsRelativePath.replace("\\", "/")
}