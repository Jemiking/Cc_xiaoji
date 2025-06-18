package com.ccxiaoji.app.data.local.dao

import androidx.room.*
import com.ccxiaoji.app.data.local.entity.SavingsContributionEntity
import com.ccxiaoji.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    // Savings Goals
    @Query("SELECT * FROM savings_goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveSavingsGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals ORDER BY isActive DESC, createdAt DESC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    suspend fun getSavingsGoalById(goalId: Long): SavingsGoalEntity?
    
    @Insert
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long
    
    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)
    
    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)
    
    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalAmount(goalId: Long, amount: Double, updatedAt: String)
    
    // Savings Contributions
    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getContributionsByGoalId(goalId: Long): Flow<List<SavingsContributionEntity>>
    
    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentContributions(goalId: Long, limit: Int): List<SavingsContributionEntity>
    
    @Insert
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long
    
    @Update
    suspend fun updateContribution(contribution: SavingsContributionEntity)
    
    @Delete
    suspend fun deleteContribution(contribution: SavingsContributionEntity)
    
    @Query("SELECT SUM(amount) FROM savings_contributions WHERE goalId = :goalId")
    suspend fun getTotalContributions(goalId: Long): Double?
    
    // Combined queries
    @Transaction
    suspend fun addContributionAndUpdateGoal(contribution: SavingsContributionEntity) {
        insertContribution(contribution)
        val currentTime = java.time.LocalDateTime.now().toString()
        updateGoalAmount(contribution.goalId, contribution.amount, currentTime)
    }
    
    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    fun getSavingsGoalByIdSync(goalId: Long): SavingsGoalEntity?
    
    @Query("SELECT * FROM savings_contributions WHERE id = :contributionId")
    fun getContributionByIdSync(contributionId: Long): SavingsContributionEntity?
}