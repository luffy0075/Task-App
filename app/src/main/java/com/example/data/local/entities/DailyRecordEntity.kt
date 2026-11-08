package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val screenTimeMinutes: Int = 0,
    val studyMinutes: Int = 0,
    val exerciseMinutes: Int = 0,
    val sleepMinutes: Int = 0,
    val mood: String = "Good", // "Great", "Good", "Neutral", "Low", "Stressed"
    val productivityRating: Int = 3, // 1..5
    val journalNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
