package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.WeeklyMissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyMissionDao {
    @Query("SELECT * FROM weekly_missions WHERE weekIdentifier = :week ORDER BY id ASC")
    fun getMissionsForWeek(week: String): Flow<List<WeeklyMissionEntity>>

    @Query("SELECT * FROM weekly_missions WHERE weekIdentifier = :week")
    suspend fun getMissionsForWeekDirect(week: String): List<WeeklyMissionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissions(missions: List<WeeklyMissionEntity>)

    @Update
    suspend fun updateMission(mission: WeeklyMissionEntity)

    @Query("UPDATE weekly_missions SET isClaimed = 1 WHERE id = :id")
    suspend fun claimMission(id: Long): Int

    @Query("UPDATE weekly_missions SET currentProgress = :progress, isCompleted = CASE WHEN :progress >= targetCount THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int)
}
