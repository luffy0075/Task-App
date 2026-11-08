package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: String,
    val iconName: String = "check_circle",
    val priority: String = "Medium", // "Low", "Medium", "High"
    val targetDurationMinutes: Int? = null,
    val startDate: String, // "yyyy-MM-dd"
    val dueDate: String? = null, // "yyyy-MM-dd"
    val repeatSchedule: String = "EVERY_DAY", // "EVERY_DAY", "WEEKDAYS", "WEEKLY", "ONE_TIME", "CUSTOM"
    val repeatDaysMask: Int = 127, // Bitmask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64 (127 = all days)
    val reminderTime: String? = null, // "HH:mm"
    val dailyTargetCount: Int = 1,
    val isPaused: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false,
    val isRequired: Boolean = true, // REQUIRED for streak vs OPTIONAL
    val customColorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
