package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    fun getRecordForDate(date: String): Flow<DailyRecordEntity?>

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDateDirect(date: String): DailyRecordEntity?

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllDailyRecords(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAllDailyRecordsList(): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getRecordsBetween(startDate: String, endDate: String): Flow<List<DailyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: DailyRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<DailyRecordEntity>)

    @Query("DELETE FROM daily_records WHERE date = :date")
    suspend fun deleteRecord(date: String)

    @Query("DELETE FROM daily_records")
    suspend fun deleteAllDailyRecords()
}
