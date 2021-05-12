package com.wutsi.platform.security.apikey

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.security.SecurityApi
import com.wutsi.security.dto.ApiKey
import com.wutsi.security.dto.GetApiKeyResponse
import com.wutsi.security.event.SecurityEventType
import com.wutsi.security.event.SecurityEventType.APIKEY_DELETED
import com.wutsi.security.event.SecurityEventType.APIKEY_UPDATED
import com.wutsi.stream.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ApiKeyProviderTest {
    private lateinit var api: SecurityApi

    private lateinit var context: ApiKeyContext

    private lateinit var provider: ApiKeyProvider

    private lateinit var cache: MutableMap<String, ApiKey>

    @BeforeEach
    fun setUp() {
        api = mock()
        cache = mock()
        context = mock()
        provider = ApiKeyProvider(api, context, cache)
    }

    @Test
    fun `resolve ApiKey from api`() {
        val key = createApiKey()
        doReturn(GetApiKeyResponse(key)).whenever(api).get(any())

        val result = provider.get("1")

        assertEquals(key, result)
        verify(cache).put("1", key)
    }

    @Test
    fun `resolve ApiKey from cache`() {
        val key = createApiKey()
        doReturn(key).whenever(cache).get(any())

        val result = provider.get("1")

        assertEquals(key, result)
        verify(api, never()).get(any())
    }

    @Test
    fun `on event APIKEY_UPDATED, remove from cache`() {
        val event = createEvent(APIKEY_UPDATED, "1")
        provider.onEvent(event)

        verify(cache).remove("1")
    }

    @Test
    fun `on event APIKEY_DELETED, remove from cache`() {
        val event = createEvent(APIKEY_DELETED, "1")
        provider.onEvent(event)

        verify(cache).remove("1")
    }

    @Test
    fun init() {
        doReturn("111").whenever(context).id()

        val key = createApiKey(id = "111")
        doReturn(GetApiKeyResponse(key)).whenever(api).get("111")

        provider.init()

        Thread.sleep(1000)
        verify(cache).put("111", key)
    }

    private fun createApiKey(id: String = "1") = ApiKey(
        id = id
    )

    private fun createEvent(type: SecurityEventType, id: String) = Event(
        type = type.urn,
        payload = """
            {
                "id": "$id"
            }
        """.trimIndent()
    )
}
