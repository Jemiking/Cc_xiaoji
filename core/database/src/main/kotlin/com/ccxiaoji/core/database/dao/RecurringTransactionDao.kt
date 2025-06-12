package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isEnabled = 1 ORDER BY name")
    fun getEnabledRecurringTransactions(userId: String): Flow<List<RecurringTransactionEntity>>
    
    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY isEnabled DESC, name")
    fun getAllRecurringTransactions(userId: String): Flow<List<RecurringTransactionEntity>>
    
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: String): RecurringTransactionEntity?
    
    @Query("SELECT * FROM recurring_transactions WHERE nextExecutionDate <= :currentDate AND isEnabled = 1")
    suspend fun getDueRecurringTransactions(currentDate: Long): List<RecurringTransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
    
    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
    
    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
    
    @Query("UPDATE recurring_transactions SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEnabledStatus(id: String, isEnabled: Boolean, updatedAt: Long)
    
    @Query("UPDATE recurring_transactions SET lastExecutionDate = :lastExecutionDate, nextExecutionDate = :nextExecutionDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateExecutionDates(id: String, lastExecutionDate: Long, nextExecutionDate: Long, updatedAt: Long)
    
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    fun getRecurringTransactionByIdSync(id: String): RecurringTransactionEntity?
}