package ru.kazantsev.nsmp.sdk.sources_sync

import kotlinx.serialization.json.Json
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest

/**
 * Коннектор к NSMP
 */
class SrcConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun checkStatus(response: ClassicHttpResponse) {
        val status = response.code
        if (status !in 200..<400) {
            val body: String? = if (response.entity != null) EntityUtils.toString(response.entity) else null
            throw BadResponseException(
                BadResponseException.createErrorText(this.host, status.toString(), body),
                status,
                response
            )
        }
    }

    fun getSrc(body : SrcRequest): ByteArray {
        val httpEntity = StringEntity(json.encodeToString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrc",
            paramsConst,
            null,
        )
        checkStatus(response)
        response.use {
            return EntityUtils.toByteArray(response.entity)
        }
    }

    fun getSrcInfo(body : SrcRequest): SrcInfoRoot {
        val httpEntity = StringEntity(json.encodeToString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrcInfo",
            paramsConst,
            null,
        )
        checkStatus(response)
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return json.decodeFromString(bodyText)
    }
}
