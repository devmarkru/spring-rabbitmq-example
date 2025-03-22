package ru.devmark.rabbitmq.example.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import ru.devmark.rabbitmq.example.dto.Status

@Entity
@Table(name = "task")
class TaskEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    var description: String,
    var status: Status,
    @OneToMany(targetEntity = SubtaskEntity::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    var subtasks: List<SubtaskEntity>,
)
