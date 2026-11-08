package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_reminders")
data class TaskReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val time: String, // "HH:mm" (24-hour format)
    val repeatDaysMask: Int = 127, // 1..127 for all days
    val label: String? = null,
    val isEnabled: Boolean = true,
    val snoozeUntilMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
