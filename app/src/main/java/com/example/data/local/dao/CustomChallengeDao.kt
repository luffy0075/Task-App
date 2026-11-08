package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CustomChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomChallengeDao {
    @Query("SELECT * FROM custom_challenges ORDER BY createdAt DESC")
    fun getAllChallenges(): Flow<List<CustomChallengeEntity>>

    @Query("SELECT * FROM custom_challenges WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveChallenges(): Flow<List<CustomChallengeEntity>>

    @Insert
    suspend fun insertChallenge(challenge: CustomChallengeEntity): Long

    @Update
    suspend fun updateChallenge(challenge: CustomChallengeEntity)

    @Query("UPDATE custom_challenges SET currentProgress = :progress, status = CASE WHEN :progress >= targetCount THEN 'COMPLETED' ELSE status END WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int)

    @Query("UPDATE custom_challenges SET isClaimed = 1 WHERE id = :id")
    suspend fun claimReward(id: Long): Int

    @Query("DELETE FROM custom_challenges WHERE id = :id")
    suspend fun deleteChallenge(id: Long)
}
