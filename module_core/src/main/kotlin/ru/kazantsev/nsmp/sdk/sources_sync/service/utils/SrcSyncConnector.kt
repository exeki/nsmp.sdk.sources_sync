package ru.kazantsev.nsmp.sdk.sources_sync.service.utils

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.io.entity.EntityUtils
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.data.dto.RemoteInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

/**
 * Коннектор к NSMP
 */
class SrcSyncConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"

    fun getSrc(body: SrcRequest): ByteArray {
        return this.execPost(
            newStringEntity(body),
            moduleBase + "getSrc",
            paramsConst,
            this::readBodyAsBytes
        )
    }

    fun getSrcInfo(body: SrcRequest): RemoteInfoRoot {
        val json = Json { ignoreUnknownKeys = true }
        return this.execPost(
            newStringEntity(body),
            moduleBase + "getSrcInfo",
            paramsConst
        ) { response -> json.decodeFromString<RemoteInfoRoot>(EntityUtils.toString(response.entity)) }
    }
}
