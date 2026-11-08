package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_days")
data class ChallengeDayEntity(
    @PrimaryKey
    val dayNumber: Int, // 1..30
    val phaseNumber: Int, // 1..4
    val phaseName: String,
    val title: String,
    val theme: String,
    val goalsJson: String, // Pipe or JSON-separated list of daily goals
    val isCompleted: Boolean = false,
    val completedGoalsMask: Int = 0, // Bitmask for goals completed
    val reflectionNotes: String = "",
    val completedAt: Long? = null
)
