package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_missions",
    indices = [
        Index(value = ["weekIdentifier", "missionKey"], unique = true)
    ]
)
data class WeeklyMissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekIdentifier: String, // e.g., "2026-W45"
    val missionKey: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetCount: Int,
    val currentProgress: Int = 0,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)
