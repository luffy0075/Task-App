package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.XpTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XpTransactionDao {
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<XpTransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions")
    fun getTotalXp(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions")
    suspend fun getTotalXpDirect(): Int

    @Query("SELECT COUNT(*) > 0 FROM xp_transactions WHERE sourceId = :sourceId")
    suspend fun hasTransaction(sourceId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: XpTransactionEntity): Long

    @Query("DELETE FROM xp_transactions WHERE sourceId = :sourceId")
    suspend fun deleteTransactionBySourceId(sourceId: String): Int

    @Query("SELECT * FROM xp_transactions WHERE date = :date")
    suspend fun getTransactionsForDate(date: String): List<XpTransactionEntity>
}
