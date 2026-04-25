package ru.kazantsev.nsmp.sdk.sources_sync.service

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfoFileRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

/**
 * Коннектор к NSMP
 */
class SrcSyncConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getSrc(body : SrcRequest): ByteArray {
        val httpEntity = StringEntity(json.encodeToString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrc",
            paramsConst,
            null,
        )
        BadResponseException.throwIfNotOk(this, response)
        response.use {
            return EntityUtils.toByteArray(response.entity)
        }
    }

    fun getSrcInfo(body : SrcRequest): RemoteInfoFileRoot {
        val httpEntity = StringEntity(json.encodeToString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrcInfo",
            paramsConst,
            null,
        )
        BadResponseException.throwIfNotOk(this, response)
        response.use {
            val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
            return json.decodeFromString(bodyText)
        }
    }
}
