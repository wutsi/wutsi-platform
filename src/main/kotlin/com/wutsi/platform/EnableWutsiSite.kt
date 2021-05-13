package com.wutsi.platform

import com.wutsi.platform.site.WutsiSiteConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(
    value = [
        WutsiSiteConfiguration::class
    ]
)
annotation class EnableWutsiSite
