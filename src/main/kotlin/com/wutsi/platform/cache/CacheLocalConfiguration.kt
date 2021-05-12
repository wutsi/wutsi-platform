package com.wutsi.platform.cache

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration

@Configuration
@ConditionalOnProperty(
    value = ["memcached.enabled"],
    havingValue = "false"
)
open class CacheLocalConfiguration {
    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = org.springframework.cache.support.SimpleCacheManager()
        cacheManager.setCaches(
            listOf(
                org.springframework.cache.concurrent.ConcurrentMapCache("default", true)
            )
        )
        return cacheManager
    }
}
