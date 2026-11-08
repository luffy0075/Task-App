package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.PurchasedRewardEntity
import com.example.data.local.entities.RewardItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_items ORDER BY orderIndex ASC")
    fun getAllRewardItems(): Flow<List<RewardItemEntity>>

    @Query("SELECT * FROM reward_items")
    suspend fun getAllRewardItemsList(): List<RewardItemEntity>

    @Query("SELECT * FROM purchased_rewards")
    fun getPurchasedRewards(): Flow<List<PurchasedRewardEntity>>

    @Query("SELECT itemId FROM purchased_rewards")
    suspend fun getPurchasedItemIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRewardItems(items: List<RewardItemEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: PurchasedRewardEntity)
}
