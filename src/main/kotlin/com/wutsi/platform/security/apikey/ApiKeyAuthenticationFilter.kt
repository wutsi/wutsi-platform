package com.wutsi.platform.security.apikey

import com.wutsi.platform.security.AnonymousAuthentication
import com.wutsi.security.dto.ApiKey
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiKeyAuthenticationFilter(
    private val apiProvider: ApiKeyProvider,
    requestMatcher: RequestMatcher
) : AbstractAuthenticationProcessingFilter(requestMatcher) {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication? {
        val apiKey = getApiKey(request) ?: return AnonymousAuthentication()
        return authenticationManager.authenticate(ApiKeyAuthentication(apiKey))
    }

    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authentication: Authentication) {
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun getApiKey(request: HttpServletRequest): ApiKey? {
        val key = request.getHeader("Authorization") ?: return null
        try {
            return apiProvider.get(key)
        } catch (ex: Exception) {
            throw AuthenticationServiceException("Invalid API-Key", ex)
        }
    }
}
