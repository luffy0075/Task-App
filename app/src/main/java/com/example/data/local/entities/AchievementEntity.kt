package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val category: String, // DAILY, STREAK, TASKS, CONSISTENCY, GROWTH, HIDDEN
    val title: String,
    val description: String,
    val rarity: String, // COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC
    val iconEmoji: String,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val isHidden: Boolean = false,
    val orderIndex: Int = 0
)
