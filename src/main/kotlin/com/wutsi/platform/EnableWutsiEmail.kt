package com.wutsi.platform

import com.wutsi.platform.email.WutsiEmailConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        WutsiEmailConfiguration::class
    ]
)
annotation class EnableWutsiEmail
