package com.wutsi.platform.security.apikey

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class ApiKeyAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(auth: Authentication): Authentication {
        auth.isAuthenticated = true
        return auth
    }

    override fun supports(clazz: Class<*>) = ApiKeyAuthentication::class.java == clazz
}
