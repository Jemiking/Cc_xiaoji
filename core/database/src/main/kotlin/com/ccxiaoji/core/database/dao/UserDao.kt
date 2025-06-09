package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId AND isDeleted = 0")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND isDeleted = 0 LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isDeleted = 0 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isDeleted = 1, updatedAt = :timestamp WHERE id = :userId")
    suspend fun softDeleteUser(userId: String, timestamp: Long)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}