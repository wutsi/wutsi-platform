package com.wutsi.platform.email

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.wutsi.email.EmailApi
import com.wutsi.email.EmailApiBuilder
import com.wutsi.platform.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.stream.ObjectMapperBuilder
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
open class WutsiEmailConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired private val apiKeyRequestInterceptor: ApiKeyRequestInterceptor
) {
    @Bean
    open fun emailApi(): EmailApi {
        val mapper = ObjectMapperBuilder().build()
        mapper.registerModule(ParameterNamesModule())
        mapper.registerModule(Jdk8Module())
        mapper.registerModule(JavaTimeModule())

        return EmailApiBuilder()
            .build(
                env = emailEnvironment(),
                mapper = mapper,
                interceptors = listOf(tracingRequestInterceptor, apiKeyRequestInterceptor)
            )
    }

    private fun emailEnvironment(): com.wutsi.email.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.email.Environment.PRODUCTION
        else
            com.wutsi.email.Environment.SANDBOX
}
