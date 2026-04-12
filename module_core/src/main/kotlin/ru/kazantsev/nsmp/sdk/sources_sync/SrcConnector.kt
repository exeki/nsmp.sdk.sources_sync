package ru.kazantsev.nsmp.sdk.sources_sync

import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.ConnectorHttpException
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest

/**
 * Коннектор к NSMP
 */
class SrcConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"

    private fun checkStatus(response: ClassicHttpResponse) {
        val status = response.code
        if (status !in 200..<400) {
            val body: String? = if (response.entity != null) EntityUtils.toString(response.entity) else null
            throw ConnectorHttpException(
                ConnectorHttpException.createErrorText(this.host, status.toString(), body),
                status,
                response
            )
        }
    }

    fun getSrc(body : SrcRequest): ByteArray {
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
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
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrcInfo",
            paramsConst,
            null,
        )
        checkStatus(response)
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return objectMapper.readValue(bodyText, SrcInfoRoot::class.java)
    }
}