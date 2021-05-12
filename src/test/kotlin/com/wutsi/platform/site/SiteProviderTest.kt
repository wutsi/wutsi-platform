package com.wutsi.platform.site

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.site.SiteApi
import com.wutsi.site.dto.GetSiteResponse
import com.wutsi.site.dto.Site
import com.wutsi.site.event.SiteEventType
import com.wutsi.site.event.SiteEventType.SITE_CREATED
import com.wutsi.site.event.SiteEventType.SITE_UPDATED
import com.wutsi.stream.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SiteProviderTest {
    private lateinit var api: SiteApi

    private lateinit var provider: SiteProvider

    private lateinit var cache: MutableMap<Long, Site>

    @BeforeEach
    fun setUp() {
        api = mock()
        cache = mock()
        provider = SiteProvider(api, cache)

        cache.clear()
    }

    @Test
    fun `resolve Site from api`() {
        val site = createSite(id = 1)
        doReturn(GetSiteResponse(site)).whenever(api).get(1L)

        val result = provider.get(1L)

        assertEquals(site, result)
    }

    @Test
    fun `resolve Site from cache`() {
        val site = createSite(id = 1)
        doReturn(site).whenever(cache).get(1L)

        val result = provider.get(1L)

        assertEquals(site, result)
        verify(api, never()).get(1L)
    }

    @Test
    fun `on event SITE_CREATED, site is cached`() {
        val site = createSite(id = 1)
        doReturn(GetSiteResponse(site)).whenever(api).get(1L)

        val event = createEvent(SITE_CREATED, 1L)
        provider.onEvent(event)

        verify(cache).put(1L, site)
    }

    @Test
    fun `on event SITE_UPDATED, site is removed from cache`() {
        val event = createEvent(SITE_UPDATED, 1L)
        provider.onEvent(event)

        verify(cache).remove(1L)
    }

    private fun createSite(id: Long = 1) = Site(
        id = id
    )

    private fun createEvent(type: SiteEventType, siteId: Long) = Event(
        type = type.urn,
        payload = """
            {
                "siteId": $siteId
            }
        """.trimIndent()
    )
}
