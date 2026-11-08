package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DailyQuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuestDao {
    @Query("SELECT * FROM daily_quests WHERE date = :date ORDER BY id ASC")
    fun getQuestsForDate(date: String): Flow<List<DailyQuestEntity>>

    @Query("SELECT * FROM daily_quests WHERE date = :date")
    suspend fun getQuestsForDateDirect(date: String): List<DailyQuestEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuests(quests: List<DailyQuestEntity>)

    @Update
    suspend fun updateQuest(quest: DailyQuestEntity)

    @Query("UPDATE daily_quests SET isClaimed = 1 WHERE id = :id")
    suspend fun claimQuest(id: Long): Int

    @Query("UPDATE daily_quests SET currentProgress = :progress, isCompleted = CASE WHEN :progress >= targetCount THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int)
}
