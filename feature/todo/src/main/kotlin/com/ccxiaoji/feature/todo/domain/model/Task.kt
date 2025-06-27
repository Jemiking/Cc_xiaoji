package com.ccxiaoji.feature.todo.domain.model

import kotlinx.datetime.Instant

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueAt: Instant?,
    val priority: Int, // 0: Low, 1: Medium, 2: High
    val completed: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val priorityLevel: Priority
        get() = when (priority) {
            2 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        }
}

enum class Priority(val displayName: String, val color: Long) {
    LOW("低", 0xFF4CAF50),
    MEDIUM("中", 0xFFFF9800),
    HIGH("高", 0xFFF44336)
}