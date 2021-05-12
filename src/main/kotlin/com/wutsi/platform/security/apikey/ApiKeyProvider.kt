package com.wutsi.platform.security.apikey

import com.wutsi.security.SecurityApi
import com.wutsi.security.dto.ApiKey
import com.wutsi.security.event.ApiKeyEventPayload
import com.wutsi.security.event.SecurityEventType
import com.wutsi.stream.Event
import com.wutsi.stream.ObjectMapperBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import java.util.Collections

class ApiKeyProvider(
    private val api: SecurityApi
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiKeyProvider::class.java)
    }

    val apiKeys: MutableMap<String, ApiKey> = Collections.synchronizedMap(mutableMapOf())

    fun get(id: String): ApiKey {
        var apiKey = apiKeys[id]
        if (apiKey == null) {
            apiKey = api.get(id).apiKey
            apiKeys[id] = apiKey
        }
        return apiKey
    }

    @EventListener
    fun onEvent(event: Event) {
        LOGGER.info("onEvent(${event.type}, ...)")

        if (event.type == SecurityEventType.APIKEY_DELETED.urn || event.type == SecurityEventType.APIKEY_UPDATED.urn) {
            val payload = ObjectMapperBuilder().build()
                .readValue(event.payload, ApiKeyEventPayload::class.java)

            LOGGER.info("Removing ApiKey#${payload.id} from the cache")
            apiKeys.remove(payload.id)
        }
    }
}
