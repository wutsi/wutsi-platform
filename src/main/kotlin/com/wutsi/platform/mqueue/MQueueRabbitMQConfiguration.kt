package com.wutsi.platform.mqueue

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

    @Value("\${wutsi.client-id}")
    private val clientId: String,

    @Value(value = "\${wutsi.rabbitmq.url}")
    private val url: String,

    @Value(value = "\${wutsi.rabbitmq.thread-pool-size:8}")
    private val threadPoolSize: Int,

    @Value(value = "\${wutsi.rabbitmq.max-retries:3}")
    private val maxRetries: Int,

    @Value(value = "\${wutsi.rabbitmq.queue-ttl-seconds:86400}")
    private val queueTtlSeconds: Long
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

    @Scheduled(cron = "\${wutsi.rabbitmq.replay-cron:0 */15 * * * *}")
    public fun replayDlq() {
        (eventStream() as RabbitMQEventStream).replayDlq()
    }
}
