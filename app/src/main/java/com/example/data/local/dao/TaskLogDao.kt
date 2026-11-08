package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TaskLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskLogDao {
    @Query("SELECT * FROM task_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs WHERE date = :date")
    suspend fun getLogsForDateList(date: String): List<TaskLogEntity>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId")
    suspend fun getLogsForTask(taskId: Long): List<TaskLogEntity>

    @Query("SELECT * FROM task_logs")
    fun getAllLogs(): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs")
    suspend fun getAllLogsList(): List<TaskLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: TaskLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<TaskLogEntity>)

    @Query("DELETE FROM task_logs WHERE date = :date AND taskId = :taskId")
    suspend fun deleteLog(date: String, taskId: Long)

    @Query("DELETE FROM task_logs WHERE taskId = :taskId")
    suspend fun deleteLogsForTask(taskId: Long)

    @Query("DELETE FROM task_logs")
    suspend fun deleteAllLogs()
}
