package com.wutsi.platform.cache

import net.rubyeye.xmemcached.MemcachedClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.memcached.enabled"],
    havingValue = "true"
)
open class CacheMemcachedConfiguration(
    @Value(value = "\${wutsi.memcached.username}")
    private val username: String,
    @Value(value = "\${wutsi.memcached.password}")
    private val password: String,
    @Value(value = "\${wutsi.memcached.servers}")
    private val servers: String,
    @Value(value = "\${wutsi.memcached.ttl}")
    private val ttl: Int
) {
    @Bean
    open fun memcachedClient(): MemcachedClient =
        com.wutsi.spring.memcached.MemcachedClientBuilder()
            .withServers(servers)
            .withPassword(password)
            .withUsername(username)
            .build()

    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(
            listOf(
                com.wutsi.spring.memcached.MemcachedCache("default", ttl, memcachedClient())
            )
        )
        return cacheManager
    }

    @Bean
    open fun memcachedHealthIndicator(): HealthIndicator =
        com.wutsi.spring.memcached.MemcachedHealthIndicator(memcachedClient())
}
