package com.wutsi.platform.site

import com.wutsi.site.SiteApi
import com.wutsi.site.dto.Site
import com.wutsi.site.event.SiteEventPayload
import com.wutsi.site.event.SiteEventType
import com.wutsi.stream.Event
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import java.util.Collections

class SiteProvider(
    private val api: SiteApi,
    private val cache: MutableMap<Long, Site> = Collections.synchronizedMap(mutableMapOf())
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(SiteProvider::class.java)
    }

    fun get(id: Long): Site {
        var site = cache[id]
        if (site == null) {
            site = api.get(id).site
            cache.put(id, site)
        }
        return site
    }

    @EventListener
    fun onEvent(event: Event) {
        LOGGER.info("onEvent(${event.type}, ...)")

        if (event.type == SiteEventType.SITE_UPDATED.urn) {
            val payload = event.payloadAs(SiteEventPayload::class.java)

            LOGGER.info("Removing Site#${payload.siteId} from the cache")
            cache.remove(payload.siteId)
        } else if (event.type == SiteEventType.SITE_CREATED.urn) {
            val payload = event.payloadAs(SiteEventPayload::class.java)

            LOGGER.info("Caching Site#${payload.siteId}")
            get(payload.siteId)
        }
    }
}
