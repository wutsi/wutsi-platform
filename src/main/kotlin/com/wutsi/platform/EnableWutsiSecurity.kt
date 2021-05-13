package com.wutsi.platform

import com.wutsi.platform.security.WutsiSecurityConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        WutsiSecurityConfiguration::class
    ]
)
annotation class EnableWutsiSecurity
