package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "xp_transactions",
    indices = [
        Index(value = ["sourceId"], unique = true)
    ]
)
data class XpTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceType: String, // TASK_COMPLETION, ALL_REQUIRED_BONUS, DAILY_QUEST, WEEKLY_MISSION, ACHIEVEMENT, CHALLENGE
    val sourceId: String,   // Unique identifier for anti-duplication (e.g. "task_1_2026-11-05")
    val amount: Int,
    val description: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
