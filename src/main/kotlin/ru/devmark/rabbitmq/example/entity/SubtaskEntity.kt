package ru.devmark.rabbitmq.example.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.devmark.rabbitmq.example.dto.Status

@Entity
@Table(name = "subtask")
class SubtaskEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    @Column(name = "task_id")
    var taskId: Int,
    var status: Status,
    var description: String,
)
