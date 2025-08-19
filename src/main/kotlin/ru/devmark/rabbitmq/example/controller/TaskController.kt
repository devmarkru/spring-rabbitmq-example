package ru.devmark.rabbitmq.example.controller

import mu.KotlinLogging
import org.springframework.amqp.core.Binding
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.devmark.rabbitmq.example.config.Exchanges
import ru.devmark.rabbitmq.example.config.RoutingKeys
import ru.devmark.rabbitmq.example.dto.EmptyResponse
import ru.devmark.rabbitmq.example.dto.Status
import ru.devmark.rabbitmq.example.dto.StatusDto
import ru.devmark.rabbitmq.example.dto.TaskDto
import ru.devmark.rabbitmq.example.entity.SubtaskEntity
import ru.devmark.rabbitmq.example.entity.TaskEntity
import ru.devmark.rabbitmq.example.event.RabbitEvent
import ru.devmark.rabbitmq.example.repository.SubtaskRepository
import ru.devmark.rabbitmq.example.repository.TaskRepository

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitAdmin: RabbitAdmin,
    private val notificationBinding: Binding,
) {

    @PostMapping
    fun createTask(@RequestBody task: TaskDto): TaskEntity {
        val entity = TaskEntity(
            status = Status.NEW,
            subtasks = emptyList(),
            description = task.description,
        )
        return taskRepository.save(entity)
            .also { logger.info { "New task with id=${it.id} created." } }
    }

    @PostMapping("/{taskId}/subtasks")
    fun createSubtask(@PathVariable taskId: Int, @RequestBody subtask: TaskDto): SubtaskEntity {
        val entity = SubtaskEntity(
            taskId = taskId,
            status = Status.NEW,
            description = subtask.description,
        )
        val modified = subtaskRepository.save(entity)
        logger.info { "New subtask with id=${modified.id} created." }
        rabbitTemplate.convertAndSend(
            Exchanges.SUBTASK_STATUS_EXCHANGE,
            RoutingKeys.SUBTASK_STATUS,
            RabbitEvent(modified.id)
        )
        return modified
    }

    @GetMapping("/{taskId}")
    fun getTaskWithSubtasks(@PathVariable taskId: Int): TaskEntity {
        return taskRepository.findById(taskId).get()
    }

    @PatchMapping("/{taskId}/subtasks/{subtaskId}")
    fun changeSubtaskStatus(
        @PathVariable taskId: Int,
        @PathVariable subtaskId: Int,
        @RequestBody dto: StatusDto,
    ): SubtaskEntity {
        val subtask = subtaskRepository.findById(subtaskId).get()
        subtask.status = dto.status
        val modified = subtaskRepository.save(subtask)
        rabbitTemplate.convertAndSend(
            Exchanges.SUBTASK_STATUS_EXCHANGE,
            RoutingKeys.SUBTASK_STATUS,
            RabbitEvent(modified.id)
        )
        return modified
    }

    @PutMapping("/notifications")
    fun activateNotifications(): EmptyResponse {
        rabbitAdmin.declareBinding(notificationBinding)
        logger.info { "Task status notifications activated." }
        return EmptyResponse()
    }

    @DeleteMapping("/notifications")
    fun deactivateNotifications(): EmptyResponse {
        rabbitAdmin.removeBinding(notificationBinding)
        logger.info { "Task status notifications deactivated." }
        return EmptyResponse()
    }
}
