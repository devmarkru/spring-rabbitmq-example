package ru.devmark.rabbitmq.example.listener

import mu.KLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.devmark.rabbitmq.example.config.Exchanges
import ru.devmark.rabbitmq.example.config.Queues
import ru.devmark.rabbitmq.example.config.RoutingKeys
import ru.devmark.rabbitmq.example.dto.Status
import ru.devmark.rabbitmq.example.entity.TaskEntity
import ru.devmark.rabbitmq.example.event.RabbitEvent
import ru.devmark.rabbitmq.example.repository.SubtaskRepository
import ru.devmark.rabbitmq.example.repository.TaskRepository

@Component
class SubtaskStatusListener(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
    private val rabbitTemplate: RabbitTemplate,
) {
    @RabbitListener(queues = [Queues.SUBTASK_STATUS_QUEUE])
    fun onSubtaskStatusChanged(event: RabbitEvent<Int>) {
        try {
            val subtaskId = event.payload
            logger.info { "Subtask status change event received (subtaskId=$subtaskId)." }
            val subtask = subtaskRepository.findById(subtaskId).get()
            val task = taskRepository.findById(subtask.taskId).get()
            val actualTaskStatus = getActualTaskStatus(task)
            if (task.status != actualTaskStatus) {
                task.status = actualTaskStatus
                val modifiedTask = taskRepository.save(task)
                logger.info { "New task status is $actualTaskStatus for taskId=${task.id}." }
                rabbitTemplate.convertAndSend(Exchanges.TASK_STATUS_EXCHANGE, RoutingKeys.TASK_STATUS, RabbitEvent(modifiedTask.id))
            }
        } catch (e: Exception) {
            logger.info { "Error while receiving subtask status change event: $e" }
        }
    }

    private fun getActualTaskStatus(task: TaskEntity) = when {
        task.subtasks.all { it.status == Status.NEW } -> Status.NEW
        task.subtasks.any { it.status in setOf(Status.NEW, Status.IN_PROGRESS) } -> Status.IN_PROGRESS
        task.subtasks.all { it.status == Status.COMPLETED } -> Status.COMPLETED
        else -> task.status
    }

    private companion object : KLogging()
}
