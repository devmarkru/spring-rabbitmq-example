package ru.devmark.rabbitmq.example.listener

import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import ru.devmark.rabbitmq.example.config.Queues
import ru.devmark.rabbitmq.example.event.RabbitEvent
import ru.devmark.rabbitmq.example.repository.TaskRepository

private val logger = KotlinLogging.logger {}

@Component
class NotificationListener(
    private val taskRepository: TaskRepository,
) {

    @RabbitListener(queues = [Queues.NOTIFICATION_QUEUE])
    fun onNotificationEvent(event: RabbitEvent<Int>) {
        try {
            logger.info { "Task status change event received: ${event.payload}." }
            val task = taskRepository.findById(event.payload).get()
            // notification logic
            logger.info { "New status for task '${task.description}' is ${task.status}." }
            logger.info { "Task status change notification sent." }
        } catch (e: Exception) {
            logger.info { "Error while receiving task status change event: $e" }
        }
    }
}
