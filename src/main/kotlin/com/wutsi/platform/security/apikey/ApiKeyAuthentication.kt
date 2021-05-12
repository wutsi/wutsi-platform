package com.wutsi.platform.security.apikey

import com.wutsi.security.dto.ApiKey
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ApiKeyAuthentication(private val apiKey: ApiKey) : Authentication {
    private val authorities: MutableCollection<SimpleGrantedAuthority>
    private var authenticated: Boolean = false

    init {
        authorities = apiKey.scopes
            .map { SimpleGrantedAuthority(it) }
            .toMutableList()
    }

    override fun getName(): String = apiKey.id

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    override fun getCredentials(): Any = ""

    override fun getDetails(): Any = apiKey

    override fun getPrincipal(): Any = apiKey.id

    override fun isAuthenticated(): Boolean = authenticated

    override fun setAuthenticated(authenticated: Boolean) {
        this.authenticated = authenticated
    }
}
