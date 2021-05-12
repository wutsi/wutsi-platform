package com.wutsi.platform.tracing

import com.wutsi.tracing.TracingContext
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.`annotation`.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.`annotation`.Bean
import org.springframework.context.`annotation`.Configuration
import javax.servlet.Filter

@Configuration
open class WutsiTracingConfiguration(
    @Autowired
    private val context: ApplicationContext,

    @Value("\${wutsi.client-id}")
    private val clientId: String
) {
    @Bean
    open fun tracingFilter(): Filter = com.wutsi.tracing.TracingFilter(tracingContext())

    @Bean
    open fun tracingContext(): TracingContext = com.wutsi.tracing.DynamicTracingContext(context)

    @Bean
    open fun tracingRequestInterceptor(): TracingRequestInterceptor =
        TracingRequestInterceptor(clientId, tracingContext())
}
