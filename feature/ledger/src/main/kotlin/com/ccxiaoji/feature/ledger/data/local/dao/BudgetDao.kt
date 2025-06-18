package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND isDeleted = 0")
    fun getBudgetsByUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month AND isDeleted = 0")
    fun getBudgetsByMonth(userId: String, year: Int, month: Int): Flow<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND isDeleted = 0")
    fun getAllBudgetsByUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month AND categoryId IS NULL AND isDeleted = 0 LIMIT 1")
    suspend fun getTotalBudget(userId: String, year: Int, month: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month AND categoryId = :categoryId AND isDeleted = 0 LIMIT 1")
    suspend fun getCategoryBudget(userId: String, year: Int, month: Int, categoryId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE id = :budgetId AND isDeleted = 0")
    suspend fun getBudgetById(budgetId: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: String, updatedAt: Long)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgetsByUser(userId: String)

    // Query to get budget with spent amount
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT b.*, 
        COALESCE(SUM(t.amountCents), 0) as spentAmountCents
        FROM budgets b
        LEFT JOIN transactions t ON t.userId = b.userId 
            AND t.categoryId = b.categoryId 
            AND t.isDeleted = 0
            AND strftime('%Y', datetime(t.createdAt/1000, 'unixepoch')) = CAST(b.year AS TEXT)
            AND strftime('%m', datetime(t.createdAt/1000, 'unixepoch')) = printf('%02d', b.month)
        WHERE b.userId = :userId 
            AND b.year = :year 
            AND b.month = :month 
            AND b.categoryId = :categoryId
            AND b.isDeleted = 0
        GROUP BY b.id
    """)
    suspend fun getBudgetWithSpent(userId: String, year: Int, month: Int, categoryId: String): BudgetWithSpent?

    // Query to get total budget with total spent amount
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT b.*, 
        COALESCE(SUM(t.amountCents), 0) as spentAmountCents
        FROM budgets b
        LEFT JOIN transactions t ON t.userId = b.userId 
            AND t.isDeleted = 0
            AND strftime('%Y', datetime(t.createdAt/1000, 'unixepoch')) = CAST(b.year AS TEXT)
            AND strftime('%m', datetime(t.createdAt/1000, 'unixepoch')) = printf('%02d', b.month)
        WHERE b.userId = :userId 
            AND b.year = :year 
            AND b.month = :month 
            AND b.categoryId IS NULL
            AND b.isDeleted = 0
        GROUP BY b.id
    """)
    suspend fun getTotalBudgetWithSpent(userId: String, year: Int, month: Int): BudgetWithSpent?

    // Query to get all budgets with spent amounts for a month
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT b.*, 
        COALESCE(SUM(t.amountCents), 0) as spentAmountCents
        FROM budgets b
        LEFT JOIN transactions t ON t.userId = b.userId 
            AND (b.categoryId IS NULL OR t.categoryId = b.categoryId)
            AND t.isDeleted = 0
            AND strftime('%Y', datetime(t.createdAt/1000, 'unixepoch')) = CAST(b.year AS TEXT)
            AND strftime('%m', datetime(t.createdAt/1000, 'unixepoch')) = printf('%02d', b.month)
        WHERE b.userId = :userId 
            AND b.year = :year 
            AND b.month = :month 
            AND b.isDeleted = 0
        GROUP BY b.id
        ORDER BY b.categoryId IS NULL DESC, b.categoryId
    """)
    fun getBudgetsWithSpent(userId: String, year: Int, month: Int): Flow<List<BudgetWithSpent>>
    
    @Query("SELECT * FROM budgets WHERE id = :budgetId AND isDeleted = 0")
    fun getBudgetByIdSync(budgetId: String): BudgetEntity?
}

data class BudgetWithSpent(
    val id: String,
    val userId: String,
    val year: Int,
    val month: Int,
    val categoryId: String?,
    val budgetAmountCents: Int,
    val alertThreshold: Float,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val spentAmountCents: Int
)