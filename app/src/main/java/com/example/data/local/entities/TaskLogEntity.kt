package com.example.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "task_logs",
    primaryKeys = ["date", "taskId"]
)
data class TaskLogEntity(
    val date: String, // "yyyy-MM-dd"
    val taskId: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null
)
