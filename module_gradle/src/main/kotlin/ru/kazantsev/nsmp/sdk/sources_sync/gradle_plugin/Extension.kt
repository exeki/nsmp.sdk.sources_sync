package ru.kazantsev.nsmp.sdk.sources_sync.gradle_plugin

import org.gradle.api.Project
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams

open class Extension(val project: Project) {
    internal var installation: Installation? = null
    internal var srcFoldersParams: SrcFoldersParams? = null

    @Suppress("unused")
    fun setSrcFoldersParams(
        scriptsRelativePath: String,
        modulesRelativePath: String,
        advImportsRelativePath: String,
    ) {
        srcFoldersParams = SrcFoldersParams(
            project.projectDir.path,
            scriptsRelativePath,
            modulesRelativePath,
            advImportsRelativePath,
        )
    }

    @Suppress("unused")
    fun setInstallation(installationId: String) {
        installation = InstallationByConfigFile(installationId)
    }

    @Suppress("unused")
    fun setInstallation(installationId: String, connectorParamsPath: String) {
        installation = InstallationByConfigFileInPath(
            installationId,
            connectorParamsPath
        )
    }

    @Suppress("unused")
    fun setInstallation(
        installationId: String,
        scheme: String,
        host: String,
        accessKey: String,
        ignoreSsl: Boolean
    ) {
        installation = InstallationDirect(
            installationId,
            scheme,
            host,
            accessKey,
            ignoreSsl
        )
    }
}

abstract class Installation {
    abstract val installationId: String
    abstract val connectorParams: ConnectorParams
    abstract fun createConnectorParams(): ConnectorParams
}

internal class InstallationByConfigFile(
    override val installationId: String
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams.byConfigFile(installationId)
    }
}

internal class InstallationByConfigFileInPath(
    override val installationId: String,
    val pathToConfigFile: String
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams.byConfigFileInPath(installationId, pathToConfigFile)
    }
}

internal class InstallationDirect(
    override val installationId: String,
    val scheme: String,
    val host: String,
    val accessKey: String,
    val ignoreSsl: Boolean
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams(
            installationId,
            scheme,
            host,
            accessKey,
            ignoreSsl
        )
    }
}
