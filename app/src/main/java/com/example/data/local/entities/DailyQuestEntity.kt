package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_quests",
    indices = [
        Index(value = ["date", "questKey"], unique = true)
    ]
)
data class DailyQuestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val questKey: String, // e.g., "COMPLETE_3_TASKS", "COMPLETE_REQUIRED", "CATEGORY_STUDY"
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetCount: Int,
    val currentProgress: Int = 0,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)
