package com.wutsi.platform.security.apikey

import org.springframework.context.ApplicationContext
import javax.servlet.http.HttpServletRequest

open class DynamicApiKeyContext(
    private val apiKeyId: String?,
    private val context: ApplicationContext
) : ApiKeyContext {
    override fun id(): String? {
        return getHttpServletRequest()?.getHeader("Authorization") ?: apiKeyId
    }

    private fun getHttpServletRequest(): HttpServletRequest? {
        return try {
            context.getBean(HttpServletRequest::class.java)
        } catch (ex: Exception) {
            null
        }
    }
}
