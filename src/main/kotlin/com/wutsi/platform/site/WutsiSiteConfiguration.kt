package com.wutsi.platform.site

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.site.Environment.PRODUCTION
import com.wutsi.site.Environment.SANDBOX
import com.wutsi.site.SiteApi
import com.wutsi.site.SiteApiBuilder
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.disable.SiteConfiguration"],
    havingValue = "true",
    matchIfMissing = true
)
open class WutsiSiteConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired private val apiKeyRequestInterceptor: ApiKeyRequestInterceptor
) {
    @Bean
    open fun siteProvider(): SiteProvider =
        SiteProvider(siteApi())

    @Bean
    open fun siteApi(): SiteApi =
        SiteApiBuilder()
            .build(
                env = siteEnvironment(),
                mapper = mapper,
                interceptors = listOf(
                    tracingRequestInterceptor,
                    apiKeyRequestInterceptor
                )
            )

    private fun siteEnvironment(): com.wutsi.site.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            PRODUCTION
        else
            SANDBOX
}
