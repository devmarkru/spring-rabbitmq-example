package ru.devmark.rabbitmq.example.config

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MqConfig {

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jsonConverter()
        return template
    }

    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    fun jsonConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun subtaskStatusQueue() = Queue(Queues.SUBTASK_STATUS_QUEUE, true)

    @Bean
    fun subtaskStatusExchange() = TopicExchange(Exchanges.SUBTASK_STATUS_EXCHANGE, true, false)

    @Bean
    fun subtaskStatusBinding(subtaskStatusQueue: Queue, subtaskStatusExchange: Exchange) = BindingBuilder
        .bind(subtaskStatusQueue)
        .to(subtaskStatusExchange)
        .with(RoutingKeys.SUBTASK_STATUS)
        .noargs()


    @Bean
    fun taskStatusExchange() = TopicExchange(Exchanges.TASK_STATUS_EXCHANGE, true, false)

    @Bean
    fun notificationQueue() = Queue(Queues.NOTIFICATION_QUEUE, true)

    @Bean
    fun notificationBinding(notificationQueue: Queue, taskStatusExchange: Exchange) = BindingBuilder
        .bind(notificationQueue)
        .to(taskStatusExchange)
        .with(RoutingKeys.TASK_STATUS)
        .noargs()
}
