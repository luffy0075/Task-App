package com.example.data.model

import com.example.data.local.entities.CategoryEntity
import com.example.data.local.entities.TaskEntity

data class TaskWithStatus(
    val task: TaskEntity,
    val category: CategoryEntity?,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val logNotes: String?,
    val isOverdue: Boolean = false
)

enum class TaskClassification(val label: String, val subtitle: String) {
    REQUIRED("Required", "Protects daily streak"),
    OPTIONAL("Optional", "Bonus progress");

    companion object {
        fun fromBoolean(isRequired: Boolean): TaskClassification =
            if (isRequired) REQUIRED else OPTIONAL
    }
}

enum class PriorityLevel(val label: String, val colorHex: String) {
    LOW("Low", "#10B981"),
    MEDIUM("Medium", "#F59E0B"),
    HIGH("High", "#EF4444");

    companion object {
        fun fromString(str: String): PriorityLevel {
            return entries.firstOrNull { it.label.equals(str, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class RepeatScheduleType(val label: String) {
    EVERY_DAY("Every day"),
    WEEKDAYS("Specific weekdays"),
    WEEKLY("Weekly"),
    ONE_TIME("One-time task"),
    CUSTOM("Custom schedule");

    companion object {
        fun fromString(str: String): RepeatScheduleType {
            return entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: EVERY_DAY
        }
    }
}

enum class MoodType(val emoji: String, val label: String) {
    GREAT("⚡", "Great"),
    GOOD("🔥", "Good"),
    NEUTRAL("🌱", "Neutral"),
    LOW("🌧️", "Low"),
    STRESSED("⚠️", "Stressed");

    companion object {
        fun fromString(str: String): MoodType {
            return entries.firstOrNull { it.label.equals(str, ignoreCase = true) } ?: GOOD
        }
    }
}

data class StreakMilestone(
    val days: Int,
    val title: String,
    val badge: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)

data class DisciplineLevel(
    val levelNumber: Int,
    val title: String,
    val minDays: Int,
    val maxDays: Int,
    val iconEmoji: String
)
