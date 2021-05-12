package com.wutsi.platform.security.apikey

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import feign.RequestTemplate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ApiKeyRequestInterceptorTest {

    @Test
    fun apply() {
        val request = RequestTemplate()
        val context = mock<ApiKeyContext>()
        doReturn("foo").whenever(context).id()
        val interceptor = ApiKeyRequestInterceptor(context)

        interceptor.apply(request)

        assertEquals(listOf("foo"), request.headers()["Authorization"])
    }
}
