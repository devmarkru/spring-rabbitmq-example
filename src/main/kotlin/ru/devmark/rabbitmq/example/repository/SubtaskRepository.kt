package ru.devmark.rabbitmq.example.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.devmark.rabbitmq.example.entity.SubtaskEntity

@Repository
interface SubtaskRepository : CrudRepository<SubtaskEntity, Int>
