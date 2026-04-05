package ru.kazantsev.nsmp.sdk.sources_sync

import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot

/**
 * Коннектор к NSMP
 */
class SrcConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"

    fun getSrc(scripts: List<String>, modules: List<String>): ByteArray {
        val body = mapOf("scripts" to scripts, "modules" to modules)
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrc",
            paramsConst,
            null,
        )
        response.use {
            return EntityUtils.toByteArray(response.entity)
        }
    }

    fun getSrcInfo(scripts: List<String>, modules: List<String>): SrcInfoRoot {
        val body = mapOf("scripts" to scripts, "modules" to modules)
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrcInfo",
            paramsConst,
            null,
        )
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return objectMapper.readValue(bodyText, SrcInfoRoot::class.java)
    }
}