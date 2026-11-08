package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_items")
data class RewardItemEntity(
    @PrimaryKey
    val id: String,
    val type: String, // PROFILE_FRAME, FLAME_STYLE, THEME, PROFILE_TITLE
    val title: String,
    val description: String,
    val iconEmoji: String,
    val costSparks: Int,
    val previewColorHex: String = "#8B5CF6",
    val orderIndex: Int = 0
)

@Entity(
    tableName = "purchased_rewards",
    primaryKeys = ["itemId"]
)
data class PurchasedRewardEntity(
    val itemId: String,
    val purchasedAt: Long = System.currentTimeMillis()
)
