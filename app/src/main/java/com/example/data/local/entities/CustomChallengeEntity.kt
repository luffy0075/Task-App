package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_challenges")
data class CustomChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val targetMetric: String, // "TOTAL_TASKS", "REQUIRED_STREAK", "CATEGORY_TASKS"
    val targetCategoryId: String? = null,
    val targetCount: Int,
    val currentProgress: Int = 0,
    val xpReward: Int,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, FAILED
    val colorHex: String = "#8B5CF6",
    val isClaimed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
