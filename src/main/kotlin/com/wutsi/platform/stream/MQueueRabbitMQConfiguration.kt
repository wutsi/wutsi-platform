package com.wutsi.blog.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.EventStream
import com.wutsi.stream.rabbitmq.RabbitMQEventStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ExecutorService

@Configuration
@ConditionalOnProperty(
    value = ["wutsi.rabbitmq.enabled"],
    havingValue = "true"
)
open class MQueueRabbitMQConfiguration(
    @Autowired
    private val eventPublisher: ApplicationEventPublisher,

    @Value(value = "\${rabbitmq.url}")
    private val url: String,

    @Value(value = "\${rabbitmq.thread-pool-size}")
    private val threadPoolSize: Int,

    @Value(value = "\${rabbitmq.max-retries}")
    private val maxRetries: Int,

    @Value(value = "\${rabbitmq.queue-ttl-seconds}")
    private val queueTtlSeconds: Long,

    @Value("\${wutsi.client-id}")
    private val clientId: String
) {
    @Bean
    open fun connectionFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.setUri(url)
        return factory
    }

    @Bean(destroyMethod = "shutdown")
    open fun executorService(): ExecutorService =
        java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize)

    @Bean(destroyMethod = "close")
    open fun channel(): Channel = connectionFactory()
        .newConnection(executorService())
        .createChannel()

    @Bean(destroyMethod = "close")
    open fun eventStream(): EventStream = RabbitMQEventStream(
        name = clientId,
        channel = channel(),
        queueTtlSeconds = queueTtlSeconds,
        maxRetries = maxRetries,
        handler = object : EventHandler {
            override fun onEvent(event: Event) {
                eventPublisher.publishEvent(event)
            }
        }
    )

    @Bean
    open fun rabbitMQHealthIndicator(): HealthIndicator =
        com.wutsi.stream.rabbitmq.RabbitMQHealthIndicator(channel())

    @Scheduled(cron = "\${rabbitmq.replay-cron}")
    public fun replayDlq() {
        (eventStream() as RabbitMQEventStream).replayDlq()
    }
}
