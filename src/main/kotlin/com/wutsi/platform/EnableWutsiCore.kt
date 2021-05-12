package com.wutsi.platform

import com.wutsi.platform.cache.CacheLocalConfiguration
import com.wutsi.platform.cache.CacheMemcachedConfiguration
import com.wutsi.platform.mqueue.MQueueLocalConfiguration
import com.wutsi.platform.mqueue.MQueueRabbitMQConfiguration
import com.wutsi.platform.security.WutsiSecurityConfiguration
import com.wutsi.platform.site.WutsiSiteConfiguration
import com.wutsi.platform.tracing.WutsiTracingConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        WutsiTracingConfiguration::class,
        WutsiSiteConfiguration::class,
        MQueueLocalConfiguration::class,
        MQueueRabbitMQConfiguration::class,
        WutsiSecurityConfiguration::class,
        CacheLocalConfiguration::class,
        CacheMemcachedConfiguration::class
    ]
)
annotation class EnableWutsiCore
