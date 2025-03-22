package ru.devmark.rabbitmq.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitExampleApplication

fun main(args: Array<String>) {
    runApplication<RabbitExampleApplication>(*args)
}
