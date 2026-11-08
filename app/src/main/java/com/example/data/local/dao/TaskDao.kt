package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY id ASC")
    suspend fun getAllActiveTasksList(): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksRaw(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteTask(id: Long)

    @Query("UPDATE tasks SET isPaused = :isPaused WHERE id = :id")
    suspend fun setTaskPaused(id: Long, isPaused: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun hardDeleteTask(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
