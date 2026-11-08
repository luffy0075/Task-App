package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val durationMinutes: Int,
    val sessionType: String = "FOCUS", // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    val associatedTaskId: Long? = null,
    val associatedTaskTitle: String? = null,
    val notes: String? = null,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int = 15
)
