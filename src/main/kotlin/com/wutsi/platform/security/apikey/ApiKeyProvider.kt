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
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

class ApiKeyProvider(
    private val api: SecurityApi,
    private val context: ApiKeyContext,
    private val cache: MutableMap<String, ApiKey> = Collections.synchronizedMap(mutableMapOf())
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiKeyProvider::class.java)
    }

    fun get(id: String): ApiKey {
        var apiKey = cache[id]
        if (apiKey == null) {
            apiKey = api.get(id).apiKey
            cache.put(id, apiKey)
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
            cache.remove(payload.id)
        }
    }

    @PostConstruct
    fun init() {
        LOGGER.info("Loading API-Key")
        val id = context.id() ?: return

        val executor = Executors.newSingleThreadExecutor()
        val task = Runnable {
            get(id)
            LOGGER.info("API-Key cached")

            executor.shutdownNow()
        }
        executor.submit(task)
    }
}
