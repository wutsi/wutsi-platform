package com.wutsi.platform.site

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.site.SiteApi
import com.wutsi.site.dto.GetSiteResponse
import com.wutsi.site.dto.SearchSiteResponse
import com.wutsi.site.dto.Site
import com.wutsi.site.dto.SiteSummary
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
    }

    @Test
    fun `return all sites`() {
        val sumaries = listOf(createSiteSummary(1), createSiteSummary(2))
        doReturn(SearchSiteResponse(sumaries))
            .doReturn(SearchSiteResponse())
            .whenever(api).search(any(), any())

        val site1 = createSite(1)
        val site2 = createSite(2)
        doReturn(GetSiteResponse(site1)).whenever(api).get(1L)
        doReturn(GetSiteResponse(site2)).whenever(api).get(2L)

        provider = SiteProvider(api)
        provider.init()
        Thread.sleep(1000)

        val sites = provider.all()

        assertEquals(2, sites.size)
        assertEquals(site1, sites[0])
        assertEquals(site2, sites[1])
    }

    @Test
    fun `resolve Site from api`() {
        val site = createSite(id = 1)
        doReturn(GetSiteResponse(site)).whenever(api).get(1L)

        val result = provider.get(1L)

        assertEquals(site, result)
        verify(cache).put(1L, site)
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

    @Test
    fun `preload sites`() {
        val sumaries = listOf(createSiteSummary(1), createSiteSummary(2))
        doReturn(SearchSiteResponse(sumaries))
            .doReturn(SearchSiteResponse())
            .whenever(api).search(any(), any())

        val site1 = createSite(1)
        val site2 = createSite(2)
        doReturn(GetSiteResponse(site1)).whenever(api).get(1L)
        doReturn(GetSiteResponse(site2)).whenever(api).get(2L)

        provider.init()

        Thread.sleep(1000)
        verify(cache).put(1L, site1)
        verify(cache).put(2L, site2)
    }

    private fun createSite(id: Long = 1) = Site(
        id = id
    )

    private fun createSiteSummary(id: Long = 1) = SiteSummary(
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
