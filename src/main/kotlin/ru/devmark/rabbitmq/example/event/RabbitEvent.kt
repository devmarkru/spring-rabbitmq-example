package ru.devmark.rabbitmq.example.event

data class RabbitEvent<T>(
    val payload: T,
)
