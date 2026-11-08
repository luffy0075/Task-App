package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ChallengeDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDayDao {
    @Query("SELECT * FROM challenge_days ORDER BY dayNumber ASC")
    fun getAllChallengeDays(): Flow<List<ChallengeDayEntity>>

    @Query("SELECT * FROM challenge_days ORDER BY dayNumber ASC")
    suspend fun getAllChallengeDaysList(): List<ChallengeDayEntity>

    @Query("SELECT * FROM challenge_days WHERE dayNumber = :dayNumber LIMIT 1")
    fun getChallengeDay(dayNumber: Int): Flow<ChallengeDayEntity?>

    @Query("SELECT * FROM challenge_days WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getChallengeDayDirect(dayNumber: Int): ChallengeDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(day: ChallengeDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<ChallengeDayEntity>)

    @Update
    suspend fun update(day: ChallengeDayEntity)

    @Query("UPDATE challenge_days SET isCompleted = 0, completedGoalsMask = 0, reflectionNotes = '', completedAt = NULL")
    suspend fun resetAllChallengeProgress()
}
