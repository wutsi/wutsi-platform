package com.wutsi.platform.security.apikey

import feign.RequestInterceptor
import feign.RequestTemplate

class ApiKeyRequestInterceptor(
    private val context: ApiKeyContext
) : RequestInterceptor {
    override fun apply(request: RequestTemplate) {
        val id = context.id()
        if (!id.isNullOrEmpty())
            request.header("Authorization", context.id())
    }
}
