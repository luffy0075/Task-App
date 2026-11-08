package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val mood: String = "Good", // "Great", "Good", "Neutral", "Low", "Tough"
    val energyRating: Int = 3, // 1..5
    val gratitudeNotes: String = "",
    val reflectionNotes: String = "",
    val positiveHighlights: String = "",
    val challengesFaced: String = "",
    val tags: String = "Discipline",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
