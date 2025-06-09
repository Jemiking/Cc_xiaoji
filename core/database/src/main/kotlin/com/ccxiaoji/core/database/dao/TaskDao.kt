package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.TaskEntity
import com.ccxiaoji.core.database.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId AND isDeleted = 0 ORDER BY priority DESC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    fun getTasksByUser(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND completed = 0 AND isDeleted = 0 ORDER BY priority DESC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    fun getIncompleteTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND completed = 1 AND isDeleted = 0 ORDER BY completedAt DESC")
    fun getCompletedTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND (title LIKE :query OR description LIKE :query) AND isDeleted = 0 ORDER BY priority DESC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    fun searchTasks(userId: String, query: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueAt IS NOT NULL AND dueAt <= :timestamp AND completed = 0 AND isDeleted = 0")
    suspend fun getOverdueTasks(userId: String, timestamp: Long): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueAt >= :startTime AND dueAt < :endTime AND isDeleted = 0 ORDER BY dueAt ASC")
    fun getTasksByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE syncStatus != :syncStatus AND isDeleted = 0")
    suspend fun getUnsyncedTasks(syncStatus: SyncStatus = SyncStatus.SYNCED): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET completed = :completed, completedAt = :completedAt, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :taskId")
    suspend fun updateTaskCompletion(
        taskId: String,
        completed: Boolean,
        completedAt: Long?,
        timestamp: Long,
        syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
    )
    
    @Query("UPDATE tasks SET isDeleted = 1, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: String, timestamp: Long, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
    
    @Query("UPDATE tasks SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, syncStatus: SyncStatus)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")
    fun getTaskByIdSync(taskId: String): TaskEntity?
}