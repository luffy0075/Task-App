package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TaskReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskReminderDao {
    @Query("SELECT * FROM task_reminders ORDER BY time ASC")
    fun getAllReminders(): Flow<List<TaskReminderEntity>>

    @Query("SELECT * FROM task_reminders")
    suspend fun getAllRemindersList(): List<TaskReminderEntity>

    @Query("SELECT * FROM task_reminders WHERE taskId = :taskId ORDER BY time ASC")
    fun getRemindersForTask(taskId: Long): Flow<List<TaskReminderEntity>>

    @Query("SELECT * FROM task_reminders WHERE taskId = :taskId ORDER BY time ASC")
    suspend fun getRemindersForTaskDirect(taskId: Long): List<TaskReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: TaskReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<TaskReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: TaskReminderEntity)

    @Query("UPDATE task_reminders SET snoozeUntilMillis = :snoozeUntil WHERE id = :id")
    suspend fun snoozeReminder(id: Long, snoozeUntil: Long)

    @Query("DELETE FROM task_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("DELETE FROM task_reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersForTask(taskId: Long)
}
