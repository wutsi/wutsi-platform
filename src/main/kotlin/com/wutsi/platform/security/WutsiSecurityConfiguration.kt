package com.wutsi.platform.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.security.apikey.ApiKeyContext
import com.wutsi.platform.security.apikey.ApiKeyProvider
import com.wutsi.platform.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.platform.security.apikey.DynamicApiKeyContext
import com.wutsi.security.Environment.PRODUCTION
import com.wutsi.security.Environment.SANDBOX
import com.wutsi.security.SecurityApi
import com.wutsi.security.SecurityApiBuilder
import com.wutsi.security.event.SecurityEventStream
import com.wutsi.stream.EventStream
import com.wutsi.stream.EventSubscription
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.platform.disable.SecurityConfiguration"],
    havingValue = "true",
    matchIfMissing = true
)
open class WutsiSecurityConfiguration(
    @Autowired
    private val context: ApplicationContext,

    @Autowired
    private val env: Environment,

    @Autowired
    private val mapper: ObjectMapper,

    @Autowired
    private val tracingRequestInterceptor: TracingRequestInterceptor,

    @Autowired
    private val eventStream: EventStream,

    @Value(value = "\${wutsi.security.api-key.id}")
    private val apiKeyId: String,
) {
    @Bean
    open fun apiKeyRequestInterceptor(): ApiKeyRequestInterceptor =
        ApiKeyRequestInterceptor(apiKeyContext())

    @Bean
    open fun apiKeyContext(): ApiKeyContext = DynamicApiKeyContext(
        apiKeyId = apiKeyId,
        context = context
    )

    @Bean
    open fun apiKeyProvider(): ApiKeyProvider = ApiKeyProvider(securityApi())

    @Bean
    open fun securitySubscription(): EventSubscription =
        EventSubscription(SecurityEventStream.NAME, eventStream)

    @Bean
    open fun securityApi(): SecurityApi = SecurityApiBuilder()
        .build(
            env = securityEnvironment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                apiKeyRequestInterceptor()
            )
        )

    private fun securityEnvironment(): com.wutsi.security.Environment =
        if (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod")))
            PRODUCTION
        else
            SANDBOX
}
