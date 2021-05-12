package com.wutsi.platform.mqueue

import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.EventStream
import com.wutsi.stream.file.FileEventStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.rabbitmq.enabled"],
    havingValue = "false"
)
open class MQueueLocalConfiguration(
    @Autowired
    private val eventPublisher: ApplicationEventPublisher,

    @Value("\${wutsi.client-id}")
    private val clientId: String
) {
    @Bean(destroyMethod = "close")
    open fun eventStream(): EventStream = eventStream(
        name = clientId
    )

    private fun eventStream(name: String): EventStream = FileEventStream(
        name = name,
        root = File(
            System.getProperty("user.home") + File.separator + "tmp",
            "mqueue"
        ),
        handler = object : EventHandler {
            override fun onEvent(event: Event) {
                eventPublisher.publishEvent(event)
            }
        }
    )
}
